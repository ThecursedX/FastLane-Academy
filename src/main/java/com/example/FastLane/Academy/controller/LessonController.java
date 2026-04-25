package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.LessonDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.service.LessonService;
import com.example.FastLane.Academy.util.VarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    @Autowired
    private LessonService lessonService;

    @Autowired
    public ResponseDTO responseDTO;

    @PostMapping(value = "/scheduleLesson")
    public ResponseEntity scheduleLesson(@RequestBody LessonDTO lessonDTO){
        try {
            String res= lessonService.scheduleLesson(lessonDTO);
        if (res.equals("00")){
            responseDTO.setCode(VarList.RSP_SUCCESS);
            responseDTO.setMessage("Success");
            responseDTO.setContent(lessonDTO);
            return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
        }else if (res.equals("20")){
            responseDTO.setCode(VarList.INSTRUCTOR_CONFLICT);
            responseDTO.setMessage("Instructor Unavailable");
            responseDTO.setContent(lessonDTO);
            return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
        }else if (res.equals("21")){
            responseDTO.setCode(VarList.STUDENT_CONFLICT);
            responseDTO.setMessage("Student Unavailable");
            responseDTO.setContent(lessonDTO);
            return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
        }else{
            responseDTO.setCode(VarList.RSP_FAIL);
            responseDTO.setMessage("Error");
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
        }
    }catch (Exception ex){
        responseDTO.setCode(VarList.RSP_ERROR);
        responseDTO.setMessage(ex.getMessage());
        responseDTO.setContent(null);
        return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    }

    @GetMapping(value = "/getAllLessons")
    public ResponseEntity getAllLessons(){
        try {
            List<LessonDTO> lessonDTOList= lessonService.getAllLessons();
            responseDTO.setCode(VarList.RSP_SUCCESS);
            responseDTO.setMessage("Success");
            responseDTO.setContent(lessonDTOList);
            return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);

        }catch (Exception ex){
            responseDTO.setCode(VarList.RSP_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/deleteLesson/{lessonId}")
    public ResponseEntity deleteLesson(@PathVariable long lessonId){
        try {
            String res= lessonService.deleteLesson(lessonId);
            if (res.equals("00")){
                responseDTO.setCode(VarList.RSP_SUCCESS);
                responseDTO.setMessage("Success");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
            }else{
                responseDTO.setCode(VarList.RSP_NO_DATA_FOUND);
                responseDTO.setMessage("No Lessons Available For this ID");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e){
            responseDTO.setCode(VarList.RSP_ERROR);
            responseDTO.setMessage(e.getMessage());
            responseDTO.setContent(e);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping(value = "/updateLesson/{lessonId}")
    public ResponseEntity updateLesson(@PathVariable long lessonId, @RequestBody LessonDTO lessonDTO){
        try {
            lessonDTO.setLessonId(lessonId);

            String res= lessonService.updateLesson(lessonDTO);
            if (res.equals("09")){
                responseDTO.setCode(VarList.UPDATED_SUCCESSFULLY);
                responseDTO.setMessage("Lesson Updated");
                responseDTO.setContent(lessonDTO);
                return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
            }else if (res.equals("13")){
                responseDTO.setCode(VarList.LESSON_NOT_FOUND);
                responseDTO.setMessage("Lesson Not Found");
                responseDTO.setContent(lessonDTO);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            }else if (res.equals("21")) {
                responseDTO.setCode(VarList.STUDENT_CONFLICT);
                responseDTO.setMessage("Student Unavailable");
                responseDTO.setContent(lessonDTO);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            }else if (res.equals("20")){
                responseDTO.setCode(VarList.INSTRUCTOR_CONFLICT);
                responseDTO.setMessage("Instructor Unavailable");
                responseDTO.setContent(lessonDTO);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            }else{
                responseDTO.setCode(VarList.RSP_FAIL);
                responseDTO.setMessage("Error");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            }
        }catch (Exception ex){
            responseDTO.setCode(VarList.RSP_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(value = "/getLessonsByStudentId/{studentId}")
    public ResponseEntity getLessonsByStudentId(@PathVariable String studentId){
        try {
          List<LessonDTO> lessonDTOList = lessonService.getLessonsByStudentId(studentId);
            responseDTO.setCode(VarList.RSP_SUCCESS);
            responseDTO.setMessage("Success");
            responseDTO.setContent(lessonDTOList);
            return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
        }catch (Exception ex){
            responseDTO.setCode(VarList.RSP_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(ex);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(value = "/getLessonsByInstructorId/{instructorId}")
    public ResponseEntity getLessonsByInstructorId(@PathVariable String instructorId){
        try {
          List<LessonDTO> lessonDTOList = lessonService.getLessonsByInstructorId(instructorId);
            responseDTO.setCode(VarList.RSP_SUCCESS);
            responseDTO.setMessage("Success");
            responseDTO.setContent(lessonDTOList);
            return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
        }catch (Exception ex){
            responseDTO.setCode(VarList.RSP_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(ex);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
