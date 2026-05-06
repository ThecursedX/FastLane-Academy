package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.LessonDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Lesson;
import com.example.FastLane.Academy.repo.LessonRepo;
import com.example.FastLane.Academy.util.LessonStatus;
import com.example.FastLane.Academy.util.VarList;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LessonService {

    @Autowired
    private LessonRepo lessonRepo;

    @Autowired
    private ModelMapper modelMapper;

   public ResponseDTO  requestLesson(LessonDTO lessonDTO){
       //date validation
       String validation = validateLessonDate(lessonDTO.getDate());

       if (validation != null) {
           return new ResponseDTO(validation, "Invalid lesson date", lessonDTO);
       }

       //time slot validation
       String timeValidation = validateTimeSlot(lessonDTO.getTime());
       if (timeValidation != null) {
           return new ResponseDTO(timeValidation, "Invalid time slot selected", lessonDTO);
       }


       Lesson lesson = modelMapper.map(lessonDTO, Lesson.class);

       lesson.setStatus(LessonStatus.PENDING);
       lesson.setRequestedAt(LocalDateTime.now());

       lessonRepo.save(lesson);
       return new ResponseDTO(VarList.REQUEST_ADDED,"Lesson request added to FIFO queue",lessonDTO);

   }
    public ResponseDTO processNextLesson(){

        Optional<Lesson> optionalLesson =
                lessonRepo.findFirstByStatusOrderByRequestedAtAsc(LessonStatus.PENDING);

        if(optionalLesson.isEmpty()){
            return new ResponseDTO(VarList.NO_PENDING_REQUESTS, "No pending lesson requests",
                    null);}

        Lesson lesson = optionalLesson.get();

        String conflict = checkConflict(lesson);

        if (conflict != null) {
            lesson.setStatus(LessonStatus.REJECTED);
            lessonRepo.save(lesson);
            String message = conflict.equals(VarList.INSTRUCTOR_CONFLICT)
                    ? "Instructor Unavailable"
                    : "Student Unavailable";

            return new ResponseDTO(conflict, message, lesson);
        }

        // No conflict → schedule
        lesson.setStatus(LessonStatus.SCHEDULED);
        lessonRepo.save(lesson);

        return new ResponseDTO(
                VarList.LESSON_SCHEDULED_SUCCESSFULLY, "Lesson scheduled successfully", lesson);
    }


    public List<LessonDTO> getAllLessons() {
        return lessonRepo.findAll().stream()
                .map(lesson -> modelMapper.map(lesson, LessonDTO.class))
                .toList();

    }

    public ResponseDTO updateLesson(LessonDTO lessonDTO) {
       //date validation
        String validation = validateLessonDate(lessonDTO.getDate());

        if (validation != null) {
            return new ResponseDTO(validation, "Invalid Date", lessonDTO);}

        //time slot validation
        String timeValidation = validateTimeSlot(lessonDTO.getTime());
        if (timeValidation != null) {
            return new ResponseDTO(timeValidation, "Invalid time slot", lessonDTO);
        }

        Optional<Lesson> optionalLesson = lessonRepo.findById(lessonDTO.getLessonId());
        if (optionalLesson.isEmpty()) {
            return new ResponseDTO(
                    VarList.LESSON_NOT_FOUND,
                    "Lesson Not Found",
                    lessonDTO);
        }
        Lesson existingLesson = optionalLesson.get();

        // check instructor conflict
        boolean instructorConflict =
                lessonRepo.existsByInstructorIdAndDateAndTimeAndStatusAndLessonIdNot(
                        lessonDTO.getInstructorId(), lessonDTO.getDate(), lessonDTO.getTime(), LessonStatus.SCHEDULED, lessonDTO.getLessonId());

        if (instructorConflict) {
            return new ResponseDTO(VarList.INSTRUCTOR_CONFLICT, "Instructor Unavailable", lessonDTO);
        }

        // check student conflict (exclude current lesson)
        boolean studentConflict =
                lessonRepo.existsByStudentIdAndDateAndTimeAndStatusAndLessonIdNot(
                        lessonDTO.getStudentId(), lessonDTO.getDate(), lessonDTO.getTime(), LessonStatus.SCHEDULED, lessonDTO.getLessonId());

        if (studentConflict) {
            return new ResponseDTO(VarList.STUDENT_CONFLICT, "Student Unavailable", lessonDTO);
        }

        // update fields
        existingLesson.setInstructorId(lessonDTO.getInstructorId());
        existingLesson.setStudentId(lessonDTO.getStudentId());
        existingLesson.setDate(lessonDTO.getDate());
        existingLesson.setTime(lessonDTO.getTime());

        lessonRepo.save(existingLesson);

        return new ResponseDTO(VarList.UPDATED_SUCCESSFULLY, "Lesson Updated", lessonDTO
        );

    }

    public ResponseDTO deleteLesson(long lessonId) {
        if (lessonRepo.existsById(lessonId)) {
            lessonRepo.deleteById(lessonId);
            return  new ResponseDTO(
                    VarList.RSP_SUCCESS, "Lesson deleted successfully", null);
        } else {
            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND, "Lesson not found", null);
        }
    }
    public List<LessonDTO> getLessonsByStudentId(String studentId) {
        List<Lesson> lessons = lessonRepo.findByStudentId(studentId);
        if (lessons.isEmpty()) {
            return new ArrayList<>();
        }
        return modelMapper.map(lessons,new TypeToken<ArrayList<LessonDTO>>(){}.getType());
    }
    public List<LessonDTO> getLessonsByInstructorId(String instructorId) {
        List<Lesson> lessons = lessonRepo.findByInstructorId(instructorId);
        if (lessons.isEmpty()) {
            return new ArrayList<>();
        }
        return modelMapper.map(lessons,new TypeToken<ArrayList<LessonDTO>>(){}.getType());
    }
    public ResponseDTO getAvailableTimeSlots(LocalDate date) {

        // validate date first
        String validation = validateLessonDate(date);
        if (validation != null) {
            return new ResponseDTO(validation, "Invalid date", null);}

        // get already booked lessons
        List<Lesson> bookedLessons =
                lessonRepo.findByDateAndStatus(date, LessonStatus.SCHEDULED);

        // extract booked times
        List<LocalTime> bookedTimes = bookedLessons.stream()
                .map(Lesson::getTime)
                .toList();

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // filter available slots
        List<LocalTime> availableSlots = ALLOWED_TIME_SLOTS.stream()
                .filter(slot -> !bookedTimes.contains(slot))//remove booked
                .filter(slot ->{
                    if (date.equals(today)){
                return slot.isAfter(now);
                    }return  true;
                })
                .toList();

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Available time slots", availableSlots);
    }

    public ResponseDTO requestReschedule(Long lessonId, LessonDTO newDetails) {

        Optional<Lesson> optionalLesson = lessonRepo.findById(lessonId);

        if (optionalLesson.isEmpty()) {
            return new ResponseDTO(
                    VarList.LESSON_NOT_FOUND,
                    "Lesson not found",
                    null
            );
        }

        Lesson oldLesson = optionalLesson.get();

        // 24-hour checking
        LocalDateTime lessonDateTime =
                LocalDateTime.of(oldLesson.getDate(), oldLesson.getTime());

        if (lessonDateTime.isBefore(LocalDateTime.now().plusHours(12))) {
            return new ResponseDTO(
                    VarList.RESCHEDULE_NOT_ALLOWED,
                    "Cannot reschedule within 24 hours of lesson",
                    oldLesson
            );
        }

        // validate date & time
        String dateValidation = validateLessonDate(newDetails.getDate());
        if (dateValidation != null) {
            return new ResponseDTO(dateValidation, "Invalid date", newDetails);
        }

        String timeValidation = validateTimeSlot(newDetails.getTime());
        if (timeValidation != null) {
            return new ResponseDTO(timeValidation, "Invalid time slot", newDetails);
        }

        // mark old lesson
        oldLesson.setStatus(LessonStatus.RESHEDULED); // or CANCELLED
        lessonRepo.save(oldLesson);

        // create new request (FIFO)
        Lesson newLesson = new Lesson();
        newLesson.setStudentId(oldLesson.getStudentId());
        newLesson.setInstructorId(newDetails.getInstructorId());
        newLesson.setDate(newDetails.getDate());
        newLesson.setTime(newDetails.getTime());
        newLesson.setStatus(LessonStatus.PENDING);
        newLesson.setRequestedAt(LocalDateTime.now());

        lessonRepo.save(newLesson);

        return new ResponseDTO(
                VarList.REQUEST_ADDED, "Reschedule request added to queue", newLesson);
    }


    public ResponseDTO cancelLesson(Long lessonId) {

        Optional<Lesson> optionalLesson = lessonRepo.findById(lessonId);

        if (optionalLesson.isEmpty()) {
            return new ResponseDTO(
                    VarList.LESSON_NOT_FOUND, "Lesson not found", null);
        }

        Lesson lesson = optionalLesson.get();

        // Only allow cancelling SCHEDULED lessons
        if (lesson.getStatus()!=LessonStatus.SCHEDULED) {
            return new ResponseDTO(
                    VarList.RSP_FAIL, "Only scheduled lessons can be cancelled", lesson);
        }

        // 24-hour rule
        LocalDateTime lessonDateTime =
                LocalDateTime.of(lesson.getDate(), lesson.getTime());

        if (lessonDateTime.isBefore(LocalDateTime.now().plusHours(24))) {
            return new ResponseDTO(
                    VarList.CANCELLATION_NOT_ALLOWED, "Cannot cancel within 24 hours of lesson", lesson);
        }

        // Cancel lesson
        lesson.setStatus(LessonStatus.CANCELLED);
        lessonRepo.save(lesson);

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Lesson cancelled successfully", lesson);
    }
    public ResponseDTO getUpcomingLessons(String studentId) {

        List<LessonDTO> list = lessonRepo
                .findByStudentIdAndStatusAndDateGreaterThanEqual(
                        studentId, LessonStatus.SCHEDULED, LocalDate.now()
                )
                .stream()
                .map(l -> modelMapper.map(l, LessonDTO.class))
                .toList();

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Upcoming lessons", list);
    }

    public ResponseDTO getLessonHistory(String studentId) {

        List<LessonDTO> list = lessonRepo
                .findByStudentIdAndDateBefore(studentId, LocalDate.now()
                )
                .stream()
                .map(l -> modelMapper.map(l, LessonDTO.class))
                .toList();

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Lesson history", list);
    }
    public ResponseDTO getLessonById(Long lessonId) {

        Optional<Lesson> optionalLesson = lessonRepo.findById(lessonId);

        if (optionalLesson.isEmpty()) {
            return new ResponseDTO(
                    VarList.LESSON_NOT_FOUND, "Lesson not found", null);
        }

        LessonDTO lessonDTO =
                modelMapper.map(optionalLesson.get(), LessonDTO.class);

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Lesson retrieved successfully", lessonDTO);
    }
    public String updateLessonStatus(Long lessonId, LessonStatus newStatus) {

        Optional<Lesson> optionalLesson = lessonRepo.findById(lessonId);

        if (optionalLesson.isEmpty()) {
            return VarList.LESSON_NOT_FOUND;
        }

        Lesson lesson = optionalLesson.get();
        LessonStatus currentStatus = lesson.getStatus();

        switch (currentStatus) {
            case PENDING:
                if (newStatus == LessonStatus.SCHEDULED || newStatus == LessonStatus.REJECTED) {
                    lesson.setStatus(newStatus);
                } else {
                    return VarList.INVALID_STATUS_CHANGE;
                }
                break;

            case SCHEDULED:
                if (newStatus == LessonStatus.COMPLETED || newStatus == LessonStatus.CANCELLED) {
                    lesson.setStatus(newStatus);
                } else {
                    return VarList.INVALID_STATUS_CHANGE;
                }
                break;

            case COMPLETED:
            case CANCELLED:
            case REJECTED:
                return VarList.STATUS_ALREADY_FINAL;
        }

        lessonRepo.save(lesson);
        return VarList.RSP_SUCCESS;
    }


    //Validations
    private String checkConflict(Lesson lesson) {

        if (lessonRepo.existsByInstructorIdAndDateAndTimeAndStatus(
                lesson.getInstructorId(),
                lesson.getDate(),
                lesson.getTime(),
                LessonStatus.SCHEDULED)) {
            return VarList.INSTRUCTOR_CONFLICT;
        }

        if (lessonRepo.existsByStudentIdAndDateAndTimeAndStatus(
                lesson.getStudentId(),
                lesson.getDate(),
                lesson.getTime(),
                LessonStatus.SCHEDULED)) {
            return VarList.STUDENT_CONFLICT;
        }

        return null;
    }

    private String validateLessonDate(LocalDate date) {

        // Block Sunday
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return VarList.INVALID_DAY; // you define this
        }

        // Block > 4 months ahead
        LocalDate maxDate = LocalDate.now().plusMonths(4);

        if (date.isAfter(maxDate)) {
            return VarList.DATE_OUT_OF_RANGE;
        }

        return null; // valid
    }
    private static final List<LocalTime> ALLOWED_TIME_SLOTS = List.of(
            LocalTime.of(8, 0),
            LocalTime.of(9, 30),
            LocalTime.of(11, 0),
            LocalTime.of(13, 0),
            LocalTime.of(14, 30)
    );
    private String validateTimeSlot(LocalTime time) {

        if (!ALLOWED_TIME_SLOTS.contains(time)) {
            return VarList.INVALID_TIME_SLOT; // define this
        }

        return null;
    }
}
