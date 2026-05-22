package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.LessonRequestDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.InstructorSlot;
import com.example.FastLane.Academy.entity.Lesson;
import com.example.FastLane.Academy.entity.LessonRequest;
import com.example.FastLane.Academy.enums.LessonStatus;
import com.example.FastLane.Academy.enums.RequestStatus;
import com.example.FastLane.Academy.repo.EnrollmentRepo;
import com.example.FastLane.Academy.repo.InstructorSlotRepo;
import com.example.FastLane.Academy.repo.LessonRepo;
import com.example.FastLane.Academy.repo.LessonRequestRepo;
import com.example.FastLane.Academy.util.VarList;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LessonRequestService {

    @Autowired private LessonRequestRepo requestRepo;
    @Autowired private InstructorSlotRepo slotRepo;
    @Autowired private LessonRepo lessonRepo;
    @Autowired private EnrollmentRepo enrollmentRepo;
    @Autowired private ModelMapper modelMapper;

    // ── Student: request a slot for a specific date ────────────────────────
    public ResponseDTO requestSlot(String studentId, LessonRequestDTO dto) {

        // Must have approved access to the course
        boolean hasAccess = enrollmentRepo
                .existsByStudentIdAndCourseIdAndAccessGrantedTrue(studentId, dto.getCourseId());
        if (!hasAccess)
            return new ResponseDTO(VarList.RSP_FAIL,
                    "Access denied. Complete enrollment and payment first.", null);

        Optional<InstructorSlot> optSlot = slotRepo.findById(dto.getSlotId());
        if (optSlot.isEmpty())
            return new ResponseDTO(VarList.SLOT_NOT_FOUND, "Slot not found", null);

        InstructorSlot slot = optSlot.get();

        // Date must match the slot's day of week
        if (!slot.getDayOfWeek().name().equals(dto.getDate().getDayOfWeek().name()))
            return new ResponseDTO(VarList.SLOT_INVALID_DAY,
                    "Date " + dto.getDate() + " is not a " + slot.getDayOfWeek(), null);

        // Date must be in the future
        if (!dto.getDate().isAfter(LocalDate.now()))
            return new ResponseDTO(VarList.INVALID_DAY, "Date must be in the future", null);

        // Guard: student already has an active (non-terminal) request for this slot+date
        List<RequestStatus> active = List.of(
                RequestStatus.PENDING, RequestStatus.IN_QUEUE,
                RequestStatus.SLOT_TAKEN, RequestStatus.SELECTED);
        List<LessonRequest> existing = requestRepo
                .findByStudentIdAndSlotIdAndDateAndStatusIn(studentId, dto.getSlotId(), dto.getDate(), active);
        if (!existing.isEmpty())
            return new ResponseDTO(VarList.STUDENT_CONFLICT,
                    "You already have an active request for this slot on that date.", null);

        LessonRequest req = new LessonRequest();
        req.setRequestId(nextRequestId());
        req.setSlotId(slot.getSlotId());
        req.setStudentId(studentId);
        req.setCourseId(dto.getCourseId());
        req.setInstructorId(slot.getInstructorId());
        req.setDate(dto.getDate());
        req.setRequestedAt(LocalDateTime.now());
        // If slot is already enabled, go straight to IN_QUEUE (not PENDING)
        req.setStatus(slot.isEnabled() ? RequestStatus.IN_QUEUE : RequestStatus.PENDING);
        req.setLessonId(null);

        requestRepo.save(req);

        // If slot is already enabled and no one is SELECTED yet for this slot+date, promote immediately
        if (slot.isEnabled()) {
            boolean noSelectedYet = requestRepo
                    .findBySlotIdAndDateAndStatusInOrderByRequestedAtAsc(
                            slot.getSlotId(), dto.getDate(), List.of(RequestStatus.SELECTED))
                    .isEmpty();
            if (noSelectedYet) {
                // This student just joined as IN_QUEUE and is the only one — select them directly
                LessonRequest saved = requestRepo.findById(req.getRequestId()).orElse(req);
                selectRequest(saved, slot);
            }
        }

        String msg = slot.isEnabled()
                ? "You are in the queue for this slot."
                : "Request received. You'll be notified when the slot is enabled.";
        return new ResponseDTO(VarList.REQUEST_ADDED, msg,
                modelMapper.map(requestRepo.findById(req.getRequestId()).orElse(req), LessonRequestDTO.class));
    }

    // ── FIFO engine: called when instructor enables a slot ─────────────────
    // For every distinct date that has PENDING requests on this slot, pick the
    // first (oldest requestedAt) → SELECTED, rest → SLOT_TAKEN.
    public void processFifoForSlot(InstructorSlot slot) {
        List<LessonRequest> pending = requestRepo
                .findBySlotIdAndStatusInOrderByRequestedAtAsc(slot.getSlotId(), List.of(RequestStatus.PENDING));

        pending.stream()
                .map(LessonRequest::getDate)
                .distinct()
                .forEach(date -> {
                    List<LessonRequest> forDate = requestRepo
                            .findBySlotIdAndDateAndStatusInOrderByRequestedAtAsc(
                                    slot.getSlotId(), date, List.of(RequestStatus.PENDING));
                    if (forDate.isEmpty()) return;

                    // First in queue → SELECTED, create lesson
                    selectRequest(forDate.get(0), slot);

                    // Rest → SLOT_TAKEN (slot is now confirmed for another student)
                    forDate.subList(1, forDate.size()).forEach(r -> {
                        r.setStatus(RequestStatus.SLOT_TAKEN);
                        requestRepo.save(r);
                    });
                });
    }

    // ── Instructor: mark lesson COMPLETE ─────────────────────────────────
    // Keep the SELECTED student's request data; delete all other requests
    // (PENDING, IN_QUEUE) for this slot+date.
    public ResponseDTO completeLesson(String instructorId, String lessonId) {
        Optional<Lesson> optLesson = lessonRepo.findById(lessonId);
        if (optLesson.isEmpty())
            return new ResponseDTO(VarList.LESSON_NOT_FOUND, "Lesson not found", null);

        Lesson lesson = optLesson.get();
        if (!lesson.getInstructorId().equals(instructorId))
            return new ResponseDTO(VarList.UNAUTHORIZED, "Not your lesson", null);

        if (lesson.getStatus() != LessonStatus.SCHEDULED)
            return new ResponseDTO(VarList.INVALID_STATUS_CHANGE,
                    "Only SCHEDULED lessons can be marked complete.", null);

        // Mark lesson complete
        lesson.setStatus(LessonStatus.COMPLETED);
        lessonRepo.save(lesson);

        // Find the SELECTED request (keep it — it holds the student's record)
        // Delete all other requests for this slot+date (PENDING and IN_QUEUE)
        String selectedRequestId = lesson.getRequestId();

        // Get the slotId from the selected request
        if (selectedRequestId != null) {
            requestRepo.findById(selectedRequestId).ifPresent(selectedReq -> {
                // Update the selected request status to reflect completion (optional but clean)
                // We leave it as SELECTED so the student's history is intact

                // Delete all non-selected, non-terminal requests for same slot+date
                List<LessonRequest> others = requestRepo
                        .findBySlotIdAndDate(selectedReq.getSlotId(), selectedReq.getDate());

                others.stream()
                        .filter(r -> !r.getRequestId().equals(selectedRequestId))
                        .filter(r -> r.getStatus() == RequestStatus.IN_QUEUE
                                || r.getStatus() == RequestStatus.SLOT_TAKEN
                                || r.getStatus() == RequestStatus.PENDING)
                        .forEach(r -> requestRepo.deleteById(r.getRequestId()));
            });
        }

        return new ResponseDTO(VarList.RSP_SUCCESS, "Lesson marked as complete. Queue cleared.", lesson);
    }

    // ── Student: cancel a PENDING / IN_QUEUE / SLOT_TAKEN / SELECTED / DISABLED request ──
    //
    //   PENDING    → free, any time
    //   IN_QUEUE   → free, any time
    //   SLOT_TAKEN → free, any time  (slot is taken by someone else — no 24h applies)
    //   DISABLED   → free, any time  (slot was disabled by instructor)
    //   SELECTED   → only if >24h before lesson start
    //   Past 24h   → blocked — student must contact instructor
    //
    public ResponseDTO cancelLesson(String studentId, String requestId) {
        Optional<LessonRequest> opt = requestRepo.findById(requestId);
        if (opt.isEmpty())
            return new ResponseDTO(VarList.REQUEST_NOT_FOUND, "Request not found", null);

        LessonRequest req = opt.get();
        if (!req.getStudentId().equals(studentId))
            return new ResponseDTO(VarList.UNAUTHORIZED, "Not your request", null);

        if (req.getStatus() == RequestStatus.CANCELLED)
            return new ResponseDTO(VarList.REQUEST_NOT_CANCELLABLE,
                    "This request is already cancelled.", null);

        RequestStatus originalStatus = req.getStatus();

        if (originalStatus == RequestStatus.SELECTED) {
            // Confirmed lesson: must cancel >24h before lesson start
            LocalTime slotTime = slotRepo.findById(req.getSlotId())
                    .map(InstructorSlot::getTime).orElse(LocalTime.of(0, 0));
            LocalDateTime lessonStart = req.getDate().atTime(slotTime);
            if (!LocalDateTime.now().isBefore(lessonStart.minusHours(24)))
                return new ResponseDTO(VarList.REQUEST_NOT_CANCELLABLE,
                        "Cannot cancel within 24 hours of the lesson. Please contact your instructor directly.", null);

            // Cancel the linked lesson record
            if (req.getLessonId() != null) {
                lessonRepo.findById(req.getLessonId()).ifPresent(l -> {
                    l.setStatus(LessonStatus.CANCELLED);
                    lessonRepo.save(l);
                });
            }
        }
        // PENDING, IN_QUEUE, SLOT_TAKEN, DISABLED: no time restriction — cancel freely

        req.setStatus(RequestStatus.CANCELLED);
        requestRepo.save(req);

        // Only promote when the confirmed student cancels and the slot is still enabled
        if (originalStatus == RequestStatus.SELECTED) {
            slotRepo.findById(req.getSlotId()).ifPresent(slot -> {
                if (slot.isEnabled()) {
                    promoteNextInQueue(req.getSlotId(), req.getDate());
                }
            });
        }

        String msg = switch (originalStatus) {
            case SELECTED   -> "Lesson cancelled. Next student in queue will be notified.";
            case SLOT_TAKEN -> "Request cancelled. The slot is held by another student. You can request a new slot.";
            case DISABLED   -> "Request cancelled. The slot was disabled by your instructor. You can request a new slot.";
            default         -> "Request cancelled. You can now pick a new slot.";
        };
        return new ResponseDTO(VarList.RSP_SUCCESS, msg,
                modelMapper.map(req, LessonRequestDTO.class));
    }

    // ── Student: reschedule ────────────────────────────────────────────────
    //   Reschedule = cancel current request so student can pick a different slot/date.
    //   DISABLED requests can be rescheduled freely (no 24h gate applies since slot is off).
    //   SELECTED requests obey the same 24h rule as cancel.
    public ResponseDTO reschedule(String studentId, String requestId) {
        return cancelLesson(studentId, requestId);
    }

    // ── Read ───────────────────────────────────────────────────────────────
    public ResponseDTO getMyRequests(String studentId) {
        List<LessonRequestDTO> list = requestRepo.findByStudentId(studentId)
                .stream().map(r -> modelMapper.map(r, LessonRequestDTO.class)).toList();
        return new ResponseDTO(VarList.RSP_SUCCESS, "Your lesson requests", list);
    }

    public ResponseDTO getRequestsForSlot(String instructorId, String slotId) {
        Optional<InstructorSlot> opt = slotRepo.findById(slotId);
        if (opt.isEmpty() || !opt.get().getInstructorId().equals(instructorId))
            return new ResponseDTO(VarList.SLOT_NOT_FOUND, "Slot not found", null);

        List<LessonRequestDTO> list = requestRepo.findBySlotId(slotId)
                .stream().map(r -> {
                    LessonRequestDTO dto = modelMapper.map(r, LessonRequestDTO.class);
                    if (r.getLessonId() != null) {
                        lessonRepo.findById(r.getLessonId())
                                .ifPresent(l -> dto.setLessonStatus(l.getStatus()));
                    }
                    return dto;
                }).toList();
        return new ResponseDTO(VarList.RSP_SUCCESS, "Requests", list);
    }

    // When the SELECTED student cancels, promote the oldest SLOT_TAKEN request.
    private void promoteNextInQueue(String slotId, LocalDate date) {
        List<LessonRequest> queue = requestRepo
                .findBySlotIdAndDateAndStatusInOrderByRequestedAtAsc(
                        slotId, date, List.of(RequestStatus.SLOT_TAKEN));
        if (!queue.isEmpty()) {
            InstructorSlot slot = slotRepo.findById(slotId).orElseThrow(
                    () -> new IllegalStateException("Slot " + slotId + " not found during promotion"));
            selectRequest(queue.get(0), slot);
        }
    }

    // Promote a request to SELECTED and create the corresponding Lesson.
    // Slot is passed in directly — avoids a redundant DB fetch and transaction visibility issues.
    // All other IN_QUEUE/SLOT_TAKEN requests for the same slot+date → SLOT_TAKEN.
    // All other queued requests the student has on the same date (different slots) → CANCELLED.
    private void selectRequest(LessonRequest req, InstructorSlot slot) {
        Lesson lesson = new Lesson();
        lesson.setLessonId(nextLessonId());
        lesson.setCourseId(req.getCourseId());
        lesson.setInstructorId(req.getInstructorId());
        lesson.setStudentId(req.getStudentId());
        lesson.setRequestId(req.getRequestId());
        lesson.setDate(req.getDate());
        lesson.setTime(slot.getTime());
        lesson.setStatus(LessonStatus.SCHEDULED);
        lessonRepo.save(lesson);

        req.setStatus(RequestStatus.SELECTED);
        req.setLessonId(lesson.getLessonId());
        requestRepo.save(req);

        // Mark every other waiting request for this slot+date as SLOT_TAKEN
        requestRepo.findBySlotIdAndDateAndStatusInOrderByRequestedAtAsc(
                        req.getSlotId(), req.getDate(),
                        List.of(RequestStatus.IN_QUEUE, RequestStatus.SLOT_TAKEN))
                .forEach(other -> {
                    other.setStatus(RequestStatus.SLOT_TAKEN);
                    requestRepo.save(other);
                });

        // Cancel all other queued requests this student has on the same date
        // (across any other slot/instructor) — one lesson per day rule
        List<RequestStatus> waitingStatuses = List.of(
                RequestStatus.PENDING, RequestStatus.IN_QUEUE, RequestStatus.SLOT_TAKEN);
        requestRepo.findByStudentIdAndDateAndStatusInAndRequestIdNot(
                        req.getStudentId(), req.getDate(), waitingStatuses, req.getRequestId())
                .forEach(other -> {
                    other.setStatus(RequestStatus.CANCELLED);
                    requestRepo.save(other);
                });
    }

    // ── ID generators ──────────────────────────────────────────────────────
    private String nextRequestId() {
        return requestRepo.findTopByOrderByRequestIdDesc()
                .map(r -> String.format("LR%03d", Integer.parseInt(r.getRequestId().substring(2)) + 1))
                .orElse("LR001");
    }

    private String nextLessonId() {
        return lessonRepo.findTopByOrderByLessonIdDesc()
                .map(l -> String.format("L%03d", Integer.parseInt(l.getLessonId().substring(1)) + 1))
                .orElse("L001");
    }
}