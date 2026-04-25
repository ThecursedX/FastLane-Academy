package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.LessonDTO;
import com.example.FastLane.Academy.entity.Lesson;
import com.example.FastLane.Academy.repo.LessonRepo;
import com.example.FastLane.Academy.util.VarList;
import org.aspectj.weaver.ast.Var;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LessonService {

    @Autowired
    private LessonRepo lessonRepo;

    @Autowired
    private ModelMapper modelMapper;

    public String scheduleLesson(LessonDTO lessonDTO){
        if (lessonRepo.findByInstructorIdAndDateAndTime(lessonDTO.getInstructorId(), lessonDTO.getDate(), lessonDTO.getTime()).isPresent()){
            return VarList.INSTRUCTOR_CONFLICT;
        } else if (lessonRepo.findByStudentIdAndDateAndTime(lessonDTO.getStudentId(),lessonDTO.getDate(),lessonDTO.getTime()).isPresent()) {
            return VarList.STUDENT_CONFLICT;
        } else{
            lessonRepo.save(modelMapper.map(lessonDTO, Lesson.class));
            return VarList.LESSON_SCHEDULED_SUCCESSFULLY;
        }

    }

    public List<LessonDTO> getAllLessons(){
        return lessonRepo.findAll().stream()
                .map(lesson -> modelMapper.map(lesson, LessonDTO.class))
                .toList();
    }

    public String updateLesson(LessonDTO lessonDTO){
        Optional<Lesson> optionalLesson = lessonRepo.findById(lessonDTO.getLessonId());
        if (optionalLesson.isEmpty()){
            return VarList.LESSON_NOT_FOUND;
        }
        // check instructor conflict
        Optional<Lesson> instructorConflict =
                lessonRepo.findByInstructorIdAndDateAndTime(
                        lessonDTO.getInstructorId(),
                        lessonDTO.getDate(),
                        lessonDTO.getTime()
                );

        if(instructorConflict.isPresent() &&
                !instructorConflict.get().getLessonId()
                        .equals(lessonDTO.getLessonId()))
        {
            return VarList.INSTRUCTOR_CONFLICT;
        }
        // check student conflict
        Optional<Lesson> studentConflict =
                lessonRepo.findByStudentIdAndDateAndTime(
                        lessonDTO.getStudentId(),
                        lessonDTO.getDate(),
                        lessonDTO.getTime()
                );

        if(studentConflict.isPresent() &&
                !studentConflict.get().getLessonId()
                        .equals(lessonDTO.getLessonId())){

            return VarList.STUDENT_CONFLICT;
        }

        // Update fields
        Lesson lesson = optionalLesson.get();

        lesson.setInstructorId(lessonDTO.getInstructorId());
        lesson.setDate(lessonDTO.getDate());
        lesson.setTime(lessonDTO.getTime());

        lessonRepo.save(lesson);

        return VarList.UPDATED_SUCCESSFULLY;
    }

    public String deleteLesson(long lessonId){
        if (lessonRepo.existsById(lessonId)){
            lessonRepo.deleteById(lessonId);
            return VarList.RSP_SUCCESS;
        }else{
            return VarList.RSP_NO_DATA_FOUND;
        }
    }
}
