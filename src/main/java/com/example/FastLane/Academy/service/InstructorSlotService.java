package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.InstructorSlotDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Instructor;
import com.example.FastLane.Academy.entity.InstructorSlot;
import com.example.FastLane.Academy.entity.LessonRequest;
import com.example.FastLane.Academy.enums.LessonStatus;
import com.example.FastLane.Academy.enums.RequestStatus;
import com.example.FastLane.Academy.repo.InstructorRepo;
import com.example.FastLane.Academy.repo.InstructorSlotRepo;
import com.example.FastLane.Academy.repo.LessonRepo;
import com.example.FastLane.Academy.repo.LessonRequestRepo;
import com.example.FastLane.Academy.util.VarList;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InstructorSlotService {

    @Autowired private InstructorSlotRepo slotRepo;
    @Autowired private InstructorRepo instructorRepo;
    @Autowired private LessonRequestRepo requestRepo;
    @Autowired private LessonRepo lessonRepo;
    @Autowired @Lazy private LessonRequestService lessonRequestService;
    @Autowired private ModelMapper modelMapper;

    private static final List<LocalTime> ALLOWED_TIMES = List.of(
            LocalTime.of(8, 0), LocalTime.of(9, 30),
            LocalTime.of(11, 0), LocalTime.of(13, 0), LocalTime.of(14, 30));

    // ── Create slot (instructor sets day + time availability) ──────────────
    public ResponseDTO createSlot(String instructorId, InstructorSlotDTO dto) {
        Optional<Instructor> opt = instructorRepo.findById(instructorId);
        if (opt.isEmpty())
            return new ResponseDTO(VarList.INSTRUCTOR_NOT_FOUND, "Instructor not found", null);

        if (!ALLOWED_TIMES.contains(dto.getTime()))
            return new ResponseDTO(VarList.INVALID_TIME_SLOT, "Invalid time slot", null);

        if (!opt.get().getWorkingDays().contains(dto.getDayOfWeek()))
            return new ResponseDTO(VarList.INSTRUCTOR_UNAVAILABLE_DAY,
                    "You don't work on " + dto.getDayOfWeek(), null);

        if (slotRepo.existsByInstructorIdAndCourseIdAndDayOfWeekAndTime(
                instructorId, dto.getCourseId(), dto.getDayOfWeek(), dto.getTime()))
            return new ResponseDTO(VarList.SLOT_ALREADY_EXISTS,
                    "You already have a slot for this course on " + dto.getDayOfWeek() + " at " + dto.getTime(), null);

        InstructorSlot slot = new InstructorSlot();
        slot.setSlotId(nextSlotId());
        slot.setInstructorId(instructorId);
        slot.setCourseId(dto.getCourseId());
        slot.setDayOfWeek(dto.getDayOfWeek());
        slot.setTime(dto.getTime());
        slot.setEnabled(false); // disabled by default; instructor enables separately

        slotRepo.save(slot);
        return new ResponseDTO(VarList.RSP_SUCCESS, "Slot created. Enable it when you're ready.",
                toDTO(slot));
    }

    // ── Enable slot → triggers FIFO selection ──────────────────────────────
    public ResponseDTO enableSlot(String instructorId, String slotId) {
        Optional<InstructorSlot> opt = slotRepo.findById(slotId);
        if (opt.isEmpty() || !opt.get().getInstructorId().equals(instructorId))
            return new ResponseDTO(VarList.SLOT_NOT_FOUND, "Slot not found", null);

        InstructorSlot slot = opt.get();
        slot.setEnabled(true);
        slotRepo.save(slot);
        slotRepo.flush(); // ensure slot is visible within the same transaction before FIFO runs

        // Trigger FIFO — process all PENDING requests for this slot
        lessonRequestService.processFifoForSlot(slot);

        return new ResponseDTO(VarList.RSP_SUCCESS, "Slot enabled. FIFO selection complete.", toDTO(slot));
    }

    // ── Disable slot ────────────────────────────────────────────────────────
    // Mark ALL active requests for this slot (PENDING, IN_QUEUE, SELECTED) as DISABLED.
    // Cancel the linked Lesson for any SELECTED request.
    // Students can then cancel or reschedule their DISABLED requests.
    public ResponseDTO disableSlot(String instructorId, String slotId) {
        Optional<InstructorSlot> opt = slotRepo.findById(slotId);
        if (opt.isEmpty() || !opt.get().getInstructorId().equals(instructorId))
            return new ResponseDTO(VarList.SLOT_NOT_FOUND, "Slot not found", null);

        // Mark all active requests as DISABLED
        List<RequestStatus> activeStatuses = List.of(
                RequestStatus.PENDING, RequestStatus.IN_QUEUE,
                RequestStatus.SLOT_TAKEN, RequestStatus.SELECTED);

        List<LessonRequest> activeRequests = requestRepo
                .findBySlotIdAndStatusInOrderByRequestedAtAsc(slotId, activeStatuses);

        for (LessonRequest req : activeRequests) {
            // If this request was SELECTED, also cancel the linked lesson
            if (req.getStatus() == RequestStatus.SELECTED && req.getLessonId() != null) {
                lessonRepo.findById(req.getLessonId()).ifPresent(lesson -> {
                    lesson.setStatus(LessonStatus.CANCELLED);
                    lessonRepo.save(lesson);
                });
            }
            req.setStatus(RequestStatus.DISABLED);
            requestRepo.save(req);
        }

        InstructorSlot slot = opt.get();
        slot.setEnabled(false);
        slotRepo.save(slot);

        int affected = activeRequests.size();
        String msg = affected > 0
                ? "Slot disabled. " + affected + " student(s) notified — they can cancel or reschedule."
                : "Slot disabled.";
        return new ResponseDTO(VarList.RSP_SUCCESS, msg, toDTO(slot));
    }

    // ── Delete slot — only if no active requests ───────────────────────────
    public ResponseDTO deleteSlot(String instructorId, String slotId) {
        Optional<InstructorSlot> opt = slotRepo.findById(slotId);
        if (opt.isEmpty() || !opt.get().getInstructorId().equals(instructorId))
            return new ResponseDTO(VarList.SLOT_NOT_FOUND, "Slot not found", null);

        boolean hasActive = requestRepo.findBySlotId(slotId).stream()
                .anyMatch(r -> r.getStatus() == RequestStatus.PENDING
                            || r.getStatus() == RequestStatus.IN_QUEUE
                            || r.getStatus() == RequestStatus.SLOT_TAKEN
                            || r.getStatus() == RequestStatus.SELECTED);
        if (hasActive)
            return new ResponseDTO(VarList.SLOT_HAS_ACTIVE_REQUESTS,
                    "Cannot delete — students are waiting. Disable the slot first.", null);

        slotRepo.deleteById(slotId);
        return new ResponseDTO(VarList.RSP_SUCCESS, "Slot deleted.", null);
    }

    // ── Read ───────────────────────────────────────────────────────────────
    public ResponseDTO getMySlots(String instructorId) {
        List<InstructorSlotDTO> list = slotRepo.findByInstructorId(instructorId)
                .stream().map(this::toDTOWithCount).toList();
        return new ResponseDTO(VarList.RSP_SUCCESS, "Slots", list);
    }

    // ── Student-facing: ALL slots for a course (enabled + disabled) ──────
    public ResponseDTO getAvailableSlotsForCourse(String courseId) {
        List<InstructorSlotDTO> slots = slotRepo.findByCourseId(courseId).stream()
                .map(this::toDTOWithCount)
                .toList();
        return new ResponseDTO(VarList.RSP_SUCCESS, "Available slots", slots);
    }

    // ── Student-facing: ALL slots sorted by instructor experience (bubble sort) ──
    public ResponseDTO getSlotsSortedByExperience(String courseId) {
        List<InstructorSlotDTO> slots = new java.util.ArrayList<>(
                slotRepo.findByCourseId(courseId).stream()
                        .map(this::toDTOWithCount)
                        .toList()
        );

        // Bubble sort: descending by experienceYears
        int n = slots.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                int expA = slots.get(j).getExperienceYears() != null ? slots.get(j).getExperienceYears() : 0;
                int expB = slots.get(j + 1).getExperienceYears() != null ? slots.get(j + 1).getExperienceYears() : 0;
                if (expA < expB) {
                    InstructorSlotDTO temp = slots.get(j);
                    slots.set(j, slots.get(j + 1));
                    slots.set(j + 1, temp);
                }
            }
        }

        return new ResponseDTO(VarList.RSP_SUCCESS, "Slots sorted by instructor experience", slots);
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private String nextSlotId() {
        return slotRepo.findTopByOrderBySlotIdDesc()
                .map(s -> String.format("IS%03d", Integer.parseInt(s.getSlotId().substring(2)) + 1))
                .orElse("IS001");
    }

    private InstructorSlotDTO toDTO(InstructorSlot s) {
        return modelMapper.map(s, InstructorSlotDTO.class);
    }

    private InstructorSlotDTO toDTOWithCount(InstructorSlot s) {
        InstructorSlotDTO dto = toDTO(s);
        int count = (int) requestRepo.findBySlotId(s.getSlotId()).stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING
                          || r.getStatus() == RequestStatus.IN_QUEUE
                          || r.getStatus() == RequestStatus.SLOT_TAKEN
                          || r.getStatus() == RequestStatus.SELECTED)
                .count();
        dto.setActiveRequestCount(count);
        instructorRepo.findById(s.getInstructorId()).ifPresent(inst -> {
            dto.setInstructorName(inst.getInstructorName());
            dto.setExperienceYears(inst.getExperienceYears());
        });
        return dto;
    }
}