package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.LessonDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Instructor;
import com.example.FastLane.Academy.entity.Lesson;
import com.example.FastLane.Academy.enums.LessonStatus;
import com.example.FastLane.Academy.enums.WorkingDay;
import com.example.FastLane.Academy.repo.EnrollmentRepo;
import com.example.FastLane.Academy.repo.InstructorRepo;
import com.example.FastLane.Academy.repo.LessonRepo;
import com.example.FastLane.Academy.util.VarList;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LessonService {

    @Autowired private LessonRepo lessonRepo;
    @Autowired private InstructorRepo instructorRepo;
    @Autowired private EnrollmentRepo enrollmentRepo;
    @Autowired private ModelMapper modelMapper;
    @Autowired @Lazy private LessonRequestService lessonRequestService;

    // ── Allowed time slots ────────────────────────────────────────────────
    private static final List<LocalTime> ALLOWED_TIME_SLOTS = List.of(
            LocalTime.of(8, 0),
            LocalTime.of(9, 30),
            LocalTime.of(11, 0),
            LocalTime.of(13, 0),
            LocalTime.of(14, 30)
    );

    // ─────────────────────────────────────────────────────────────────────
    // STUDENT: request a lesson
    // ─────────────────────────────────────────────────────────────────────
    public ResponseDTO requestLesson(LessonDTO lessonDTO) {

        boolean hasAccess = enrollmentRepo
                .existsByStudentIdAndCourseIdAndAccessGrantedTrue(
                        lessonDTO.getStudentId(),
                        lessonDTO.getCourseId()
                );

        if (!hasAccess) {
            return new ResponseDTO(VarList.RSP_FAIL,
                    "Student does not have access. Complete enrollment and payment first.",
                    lessonDTO);
        }

        String dateErr = validateLessonDate(lessonDTO.getDate());
        if (dateErr != null)
            return new ResponseDTO(dateErr, "Invalid lesson date", lessonDTO);

        String timeErr = validateTimeSlot(lessonDTO.getTime());
        if (timeErr != null)
            return new ResponseDTO(timeErr, "Invalid time slot", lessonDTO);

        Lesson lesson = modelMapper.map(lessonDTO, Lesson.class);

        String conflict = checkConflict(lesson);
        if (conflict != null) {
            return new ResponseDTO(conflict, conflictMessage(conflict), lessonDTO);
        }

        lesson.setLessonId(generateNextId());
        lesson.setStatus(LessonStatus.SCHEDULED);   // adjust if your enum differs
        lessonRepo.save(lesson);

        return new ResponseDTO(VarList.REQUEST_ADDED, "Lesson request added", lessonDTO);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ADMIN: process next pending lesson in FIFO queue
    // ─────────────────────────────────────────────────────────────────────
    public ResponseDTO processNextLesson() {

        // NOTE: add this method to LessonRepo if not present:
        // Optional<Lesson> findFirstByStatusOrderByLessonIdAsc(LessonStatus status);
        Optional<Lesson> opt = lessonRepo.findFirstByStatusOrderByLessonIdAsc(LessonStatus.SCHEDULED);

        if (opt.isEmpty())
            return new ResponseDTO(VarList.NO_PENDING_REQUESTS, "No pending lesson requests", null);

        Lesson lesson = opt.get();
        String conflict = checkConflict(lesson);

        if (conflict != null) {
            // Mark as cancelled/rejected — use whatever status your enum has
            lesson.setStatus(LessonStatus.CANCELLED);
            lessonRepo.save(lesson);
            return new ResponseDTO(conflict, conflictMessage(conflict), lesson);
        }

        lesson.setStatus(LessonStatus.SCHEDULED);
        lessonRepo.save(lesson);
        return new ResponseDTO(VarList.LESSON_SCHEDULED_SUCCESSFULLY, "Lesson scheduled", lesson);
    }

    // ─────────────────────────────────────────────────────────────────────
    // READ helpers
    // ─────────────────────────────────────────────────────────────────────
    public List<LessonDTO> getAllLessons() {
        return lessonRepo.findAll().stream()
                .map(l -> modelMapper.map(l, LessonDTO.class))
                .toList();
    }

    public ResponseDTO getLessonById(String lessonId) {
        Optional<Lesson> opt = lessonRepo.findById(lessonId);
        if (opt.isEmpty())
            return new ResponseDTO(VarList.LESSON_NOT_FOUND, "Lesson not found", null);
        return new ResponseDTO(VarList.RSP_SUCCESS, "Success",
                modelMapper.map(opt.get(), LessonDTO.class));
    }

    public List<LessonDTO> getLessonsByStudentId(String studentId) {
        List<Lesson> lessons = lessonRepo.findByStudentId(studentId);
        if (lessons.isEmpty()) return new ArrayList<>();
        return modelMapper.map(lessons, new TypeToken<ArrayList<LessonDTO>>(){}.getType());
    }

    public List<LessonDTO> getLessonsByInstructorId(String instructorId) {
        List<Lesson> lessons = lessonRepo.findByInstructorId(instructorId);
        if (lessons.isEmpty()) return new ArrayList<>();
        return modelMapper.map(lessons, new TypeToken<ArrayList<LessonDTO>>(){}.getType());
    }

    public ResponseDTO getUpcomingLessons(String studentId) {
        List<LessonDTO> list = lessonRepo
                .findByStudentIdAndStatusAndDateGreaterThanEqual(
                        studentId, LessonStatus.SCHEDULED, LocalDate.now())
                .stream()
                .map(l -> modelMapper.map(l, LessonDTO.class))
                .toList();
        return new ResponseDTO(VarList.RSP_SUCCESS, "Upcoming lessons", list);
    }

    public ResponseDTO getLessonHistory(String studentId) {
        List<LessonDTO> list = lessonRepo
                .findByStudentIdAndDateBefore(studentId, LocalDate.now())
                .stream()
                .map(l -> modelMapper.map(l, LessonDTO.class))
                .toList();
        return new ResponseDTO(VarList.RSP_SUCCESS, "Lesson history", list);
    }

    // ─────────────────────────────────────────────────────────────────────
    // UPDATE / DELETE
    // ─────────────────────────────────────────────────────────────────────
    public ResponseDTO updateLesson(LessonDTO lessonDTO) {

        String dateErr = validateLessonDate(lessonDTO.getDate());
        if (dateErr != null)
            return new ResponseDTO(dateErr, "Invalid date", lessonDTO);

        String timeErr = validateTimeSlot(lessonDTO.getTime());
        if (timeErr != null)
            return new ResponseDTO(timeErr, "Invalid time slot", lessonDTO);

        Optional<Lesson> opt = lessonRepo.findById(lessonDTO.getLessonId());
        if (opt.isEmpty())
            return new ResponseDTO(VarList.LESSON_NOT_FOUND, "Lesson not found", lessonDTO);

        Lesson lesson = opt.get();

        Optional<Instructor> instOpt = instructorRepo.findById(lessonDTO.getInstructorId());
        if (instOpt.isEmpty())
            return new ResponseDTO(VarList.INSTRUCTOR_NOT_FOUND, "Instructor not found", lessonDTO);

        DayOfWeek day = lessonDTO.getDate().getDayOfWeek();
        if (!instOpt.get().getWorkingDays().contains(WorkingDay.valueOf(day.name())))
            return new ResponseDTO(VarList.INSTRUCTOR_UNAVAILABLE_DAY,
                    "Instructor not available on this day", lessonDTO);

        // NOTE: add these to LessonRepo if not present:
        // boolean existsByInstructorIdAndDateAndTimeAndStatusAndLessonIdNot(
        //         String instructorId, LocalDate date, LocalTime time,
        //         LessonStatus status, String lessonId);
        // boolean existsByStudentIdAndDateAndTimeAndStatusAndLessonIdNot(
        //         String studentId, LocalDate date, LocalTime time,
        //         LessonStatus status, String lessonId);
        boolean instConflict = lessonRepo
                .existsByInstructorIdAndDateAndTimeAndStatusAndLessonIdNot(
                        lessonDTO.getInstructorId(), lessonDTO.getDate(), lessonDTO.getTime(),
                        LessonStatus.SCHEDULED, lessonDTO.getLessonId());
        if (instConflict)
            return new ResponseDTO(VarList.INSTRUCTOR_CONFLICT, "Instructor unavailable", lessonDTO);

        boolean stuConflict = lessonRepo
                .existsByStudentIdAndDateAndTimeAndStatusAndLessonIdNot(
                        lessonDTO.getStudentId(), lessonDTO.getDate(), lessonDTO.getTime(),
                        LessonStatus.SCHEDULED, lessonDTO.getLessonId());
        if (stuConflict)
            return new ResponseDTO(VarList.STUDENT_CONFLICT, "Student unavailable", lessonDTO);

        lesson.setInstructorId(lessonDTO.getInstructorId());
        lesson.setStudentId(lessonDTO.getStudentId());
        lesson.setDate(lessonDTO.getDate());
        lesson.setTime(lessonDTO.getTime());
        lessonRepo.save(lesson);

        return new ResponseDTO(VarList.UPDATED_SUCCESSFULLY, "Lesson updated", lessonDTO);
    }

    public ResponseDTO deleteLesson(String lessonId) {
        if (!lessonRepo.existsById(lessonId))
            return new ResponseDTO(VarList.RSP_NO_DATA_FOUND, "Lesson not found", null);
        lessonRepo.deleteById(lessonId);
        return new ResponseDTO(VarList.RSP_SUCCESS, "Lesson deleted", null);
    }

    public String updateLessonStatus(String lessonId, LessonStatus newStatus, String instructorId) {
        Optional<Lesson> opt = lessonRepo.findById(lessonId);
        if (opt.isEmpty()) return VarList.LESSON_NOT_FOUND;

        Lesson lesson = opt.get();
        LessonStatus current = lesson.getStatus();

        boolean allowed = switch (current) {
            case SCHEDULED  -> newStatus == LessonStatus.COMPLETED || newStatus == LessonStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
            default -> false;
        };

        if (!allowed) return VarList.INVALID_STATUS_CHANGE;

        // COMPLETED: delegate to LessonRequestService so queue cleanup happens
        if (newStatus == LessonStatus.COMPLETED) {
            ResponseDTO res = lessonRequestService.completeLesson(instructorId, lessonId);
            return res.getCode();
        }

        lesson.setStatus(newStatus);
        lessonRepo.save(lesson);
        return VarList.RSP_SUCCESS;
    }

    // ─────────────────────────────────────────────────────────────────────
    // RESCHEDULE / CANCEL
    // ─────────────────────────────────────────────────────────────────────
    public ResponseDTO cancelLesson(String lessonId) {
        Optional<Lesson> opt = lessonRepo.findById(lessonId);
        if (opt.isEmpty())
            return new ResponseDTO(VarList.LESSON_NOT_FOUND, "Lesson not found", null);

        Lesson lesson = opt.get();
        if (lesson.getStatus() != LessonStatus.SCHEDULED)
            return new ResponseDTO(VarList.RSP_FAIL, "Only scheduled lessons can be cancelled", lesson);

        lesson.setStatus(LessonStatus.CANCELLED);
        lessonRepo.save(lesson);
        return new ResponseDTO(VarList.RSP_SUCCESS, "Lesson cancelled", lesson);
    }

    // ─────────────────────────────────────────────────────────────────────
    // AVAILABLE SLOTS
    // ─────────────────────────────────────────────────────────────────────
    public ResponseDTO getAvailableTimeSlots(LocalDate date) {
        String err = validateLessonDate(date);
        if (err != null)
            return new ResponseDTO(err, "Invalid date", null);

        // NOTE: add this to LessonRepo if not present:
        // List<Lesson> findByDateAndStatus(LocalDate date, LessonStatus status);
        List<Lesson> booked = lessonRepo.findByDateAndStatus(date, LessonStatus.SCHEDULED);
        List<LocalTime> bookedTimes = booked.stream().map(Lesson::getTime).toList();

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<LocalTime> available = ALLOWED_TIME_SLOTS.stream()
                .filter(s -> !bookedTimes.contains(s))
                .filter(s -> !date.equals(today) || s.isAfter(now))
                .toList();

        return new ResponseDTO(VarList.RSP_SUCCESS, "Available slots", available);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────
    private String generateNextId() {
        String lastId = lessonRepo.findTopByOrderByLessonIdDesc()
                .map(Lesson::getLessonId)
                .orElse(null);
        if (lastId == null) return "L001";
        return String.format("L%03d", Integer.parseInt(lastId.substring(1)) + 1);
    }

    private String checkConflict(Lesson lesson) {
        Optional<Instructor> opt = instructorRepo.findById(lesson.getInstructorId());
        if (opt.isEmpty()) return VarList.INSTRUCTOR_NOT_FOUND;

        DayOfWeek day = lesson.getDate().getDayOfWeek();
        if (!opt.get().getWorkingDays().contains(WorkingDay.valueOf(day.name())))
            return VarList.INSTRUCTOR_UNAVAILABLE_DAY;

        // NOTE: add these to LessonRepo if not present:
        // boolean existsByInstructorIdAndDateAndTimeAndStatus(
        //         String instructorId, LocalDate date, LocalTime time, LessonStatus status);
        // boolean existsByStudentIdAndDateAndTimeAndStatus(
        //         String studentId, LocalDate date, LocalTime time, LessonStatus status);
        if (lessonRepo.existsByInstructorIdAndDateAndTimeAndStatus(
                lesson.getInstructorId(), lesson.getDate(), lesson.getTime(), LessonStatus.SCHEDULED))
            return VarList.INSTRUCTOR_CONFLICT;

        if (lessonRepo.existsByStudentIdAndDateAndTimeAndStatus(
                lesson.getStudentId(), lesson.getDate(), lesson.getTime(), LessonStatus.SCHEDULED))
            return VarList.STUDENT_CONFLICT;

        return null;
    }

    private String validateLessonDate(LocalDate date) {
        if (date == null) return VarList.INVALID_DAY;
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) return VarList.INVALID_DAY;
        if (date.isBefore(LocalDate.now())) return VarList.INVALID_DAY;
        if (date.isAfter(LocalDate.now().plusMonths(4))) return VarList.DATE_OUT_OF_RANGE;
        return null;
    }

    private String validateTimeSlot(LocalTime time) {
        if (time == null || !ALLOWED_TIME_SLOTS.contains(time)) return VarList.INVALID_TIME_SLOT;
        return null;
    }

    private String conflictMessage(String code) {
        return switch (code) {
            case VarList.INSTRUCTOR_CONFLICT       -> "Instructor already booked";
            case VarList.STUDENT_CONFLICT          -> "Student already booked";
            case VarList.INSTRUCTOR_UNAVAILABLE_DAY -> "Instructor not available on this day";
            case VarList.INSTRUCTOR_NOT_FOUND      -> "Instructor not found";
            default -> "Scheduling conflict";
        };
    }
}