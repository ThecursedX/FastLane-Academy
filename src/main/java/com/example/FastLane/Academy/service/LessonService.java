package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.LessonDTO;
import com.example.FastLane.Academy.entity.Lesson;
import com.example.FastLane.Academy.repo.LessonRepo;
import com.example.FastLane.Academy.util.VarList;
import org.aspectj.weaver.ast.Var;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

   public String requestLesson(LessonDTO lessonDTO){

       Lesson lesson = modelMapper.map(lessonDTO, Lesson.class);

       lesson.setStatus("PENDING");
       lesson.setRequestedAt(LocalDateTime.now());

       lessonRepo.save(lesson);
       return VarList.REQUEST_ADDED;
   }
    public String processNextLesson(){

        List<Lesson> pendingQueue =
                lessonRepo.findByStatusOrderByRequestedAtAsc("PENDING");

        if(pendingQueue.isEmpty()){ return VarList.NO_PENDING_REQUESTS;}

        for (Lesson lesson : pendingQueue){ //unitl the "pending" stated list stops
        // check conflicts before scheduling
           boolean instructorConflict = lessonRepo.existsByInstructorIdAndDateAndTimeAndStatusAndLessonIdNot(lesson.getInstructorId(), lesson.getDate(), lesson.getTime(),"SCHEDULED", lesson.getLessonId());
           boolean studentConflict = lessonRepo.existsByStudentIdAndDateAndTimeAndStatusAndLessonIdNot(lesson.getStudentId(), lesson.getDate(), lesson.getTime(),"SCHEDULED",lesson.getLessonId());

           // conflict? reject and continue to next request
            if(instructorConflict || studentConflict){
                lesson.setStatus("REJECTED");
                lessonRepo.save(lesson);
                continue;
            }
            lesson.setStatus("SCHEDULED");
            lessonRepo.save(lesson);
            return VarList.LESSON_SCHEDULED_SUCCESSFULLY;
            }
        // all pending requests were rejected
        return VarList.NO_PENDING_REQUESTS;
    }


    public List<LessonDTO> getAllLessons() {
        return lessonRepo.findAll().stream()
                .map(lesson -> modelMapper.map(lesson, LessonDTO.class))
                .toList();
    }

    public String updateLesson(LessonDTO lessonDTO) {
        Optional<Lesson> optionalLesson = lessonRepo.findById(lessonDTO.getLessonId());
        if (optionalLesson.isEmpty()) {
            return VarList.LESSON_NOT_FOUND;
        }
        Lesson existingLesson = optionalLesson.get();
        // check instructor conflict
        if (lessonRepo.existsByInstructorIdAndDateAndTime(lessonDTO.getInstructorId(), lessonDTO.getDate(), lessonDTO.getTime())
                &&!(existingLesson.getInstructorId().equals(lessonDTO.getInstructorId())//exclude current lesson
                && existingLesson.getDate().equals(lessonDTO.getDate())
                && existingLesson.getTime().equals(lessonDTO.getTime()))) {
            return VarList.INSTRUCTOR_CONFLICT;
        }

        // check student conflict (exclude current lesson)
        if (lessonRepo.existsByStudentIdAndDateAndTime(
                lessonDTO.getStudentId(),
                lessonDTO.getDate(),
                lessonDTO.getTime()
        ) && !(existingLesson.getStudentId().equals(lessonDTO.getStudentId())
                && existingLesson.getDate().equals(lessonDTO.getDate())
                && existingLesson.getTime().equals(lessonDTO.getTime()))) {

            return VarList.STUDENT_CONFLICT;
        }

        // update fields
        existingLesson.setInstructorId(lessonDTO.getInstructorId());
        existingLesson.setStudentId(lessonDTO.getStudentId());
        existingLesson.setDate(lessonDTO.getDate());
        existingLesson.setTime(lessonDTO.getTime());

        lessonRepo.save(existingLesson);

        return VarList.UPDATED_SUCCESSFULLY;
    }

    public String deleteLesson(long lessonId) {
        if (lessonRepo.existsById(lessonId)) {
            lessonRepo.deleteById(lessonId);
            return VarList.RSP_SUCCESS;
        } else {
            return VarList.RSP_NO_DATA_FOUND;
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

}
