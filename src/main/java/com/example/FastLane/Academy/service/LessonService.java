package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.LessonDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Lesson;
import com.example.FastLane.Academy.repo.LessonRepo;
import com.example.FastLane.Academy.util.VarList;
import org.aspectj.weaver.ast.Var;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
       String validation = validateLessonDate(lessonDTO.getDate());

       if (validation != null) {
           return new ResponseDTO(validation, "Invalid lesson date", lessonDTO);
       }

       Lesson lesson = modelMapper.map(lessonDTO, Lesson.class);

       lesson.setStatus("PENDING");
       lesson.setRequestedAt(LocalDateTime.now());

       lessonRepo.save(lesson);
       return new ResponseDTO(VarList.REQUEST_ADDED,"Lesson request added to FIFO queue",lessonDTO);

   }
    public ResponseDTO processNextLesson(){

        Optional<Lesson> optionalLesson =
                lessonRepo.findFirstByStatusOrderByRequestedAtAsc("PENDING");

        if(optionalLesson.isEmpty()){
            return new ResponseDTO(VarList.NO_PENDING_REQUESTS, "No pending lesson requests",
                    null);}

        Lesson lesson = optionalLesson.get();

        String conflict = checkConflict(lesson);

        if (conflict != null) {
            lesson.setStatus("REJECTED");
            lessonRepo.save(lesson);
            String message = conflict.equals(VarList.INSTRUCTOR_CONFLICT)
                    ? "Instructor Unavailable"
                    : "Student Unavailable";

            return new ResponseDTO(conflict, message, lesson);
        }

        // No conflict → schedule
        lesson.setStatus("SCHEDULED");
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
        String validation = validateLessonDate(lessonDTO.getDate());

        if (validation != null) {
            return new ResponseDTO(validation, "Invalid Date", lessonDTO);
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
                        lessonDTO.getInstructorId(), lessonDTO.getDate(), lessonDTO.getTime(), "SCHEDULED", lessonDTO.getLessonId());

        if (instructorConflict) {
            return new ResponseDTO(VarList.INSTRUCTOR_CONFLICT, "Instructor Unavailable", lessonDTO);
        }

        // check student conflict (exclude current lesson)
        boolean studentConflict =
                lessonRepo.existsByStudentIdAndDateAndTimeAndStatusAndLessonIdNot(
                        lessonDTO.getStudentId(), lessonDTO.getDate(), lessonDTO.getTime(), "SCHEDULED", lessonDTO.getLessonId());

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

    //Validations
    private String checkConflict(Lesson lesson) {

        if (lessonRepo.existsByInstructorIdAndDateAndTimeAndStatus(
                lesson.getInstructorId(),
                lesson.getDate(),
                lesson.getTime(),
                "SCHEDULED")) {
            return VarList.INSTRUCTOR_CONFLICT;
        }

        if (lessonRepo.existsByStudentIdAndDateAndTimeAndStatus(
                lesson.getStudentId(),
                lesson.getDate(),
                lesson.getTime(),
                "SCHEDULED")) {
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
}
