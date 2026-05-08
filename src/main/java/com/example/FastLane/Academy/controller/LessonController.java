package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.LessonDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.service.LessonService;
import com.example.FastLane.Academy.util.LessonStatus;
import com.example.FastLane.Academy.util.VarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    @Autowired
    private LessonService lessonService;

    @Autowired
    public ResponseDTO responseDTO;



    @PostMapping(value = "/requestLesson")
    public ResponseEntity<ResponseDTO> requestLesson(@RequestBody LessonDTO lessonDTO){
        ResponseDTO response = lessonService.requestLesson(lessonDTO);

        HttpStatus status = response.getCode().equals(VarList.REQUEST_ADDED)
                ? HttpStatus.ACCEPTED
                : HttpStatus.BAD_REQUEST;

        return  ResponseEntity.status(status).body(response);
    }
    @PostMapping(value = "/processNextLesson")
    public ResponseEntity<ResponseDTO> processNextLesson() {

        ResponseDTO response = lessonService.processNextLesson();

        HttpStatus status = response.getCode().equals(VarList.REQUEST_ADDED)
                ? HttpStatus.ACCEPTED
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);

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

    @GetMapping("/getLesson/{lessonId}")
    public ResponseEntity<ResponseDTO> getLessonById(@PathVariable String lessonId) {

        ResponseDTO response = lessonService.getLessonById(lessonId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping(value = "/deleteLesson/{lessonId}")
    public ResponseEntity<ResponseDTO> deleteLesson(@PathVariable String lessonId){
        ResponseDTO response =  lessonService.deleteLesson(lessonId);

        HttpStatus status = response.getCode().equals(VarList.RSP_SUCCESS)
                ? HttpStatus.ACCEPTED
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }
    @PutMapping(value = "/updateLesson/{lessonId}")
    public ResponseEntity<ResponseDTO> updateLesson(@PathVariable String lessonId, @RequestBody LessonDTO lessonDTO){
        lessonDTO.setLessonId(lessonId);
        ResponseDTO response = lessonService.updateLesson(lessonDTO);

        HttpStatus status = response.getCode().equals(VarList.UPDATED_SUCCESSFULLY)
                ? HttpStatus.ACCEPTED
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);

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

    @GetMapping(value = "/available-slots")
    public ResponseEntity<ResponseDTO> getAvailableSlots(@RequestParam LocalDate date){
        ResponseDTO response = lessonService.getAvailableTimeSlots(date);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/requestReschedule/{lessonId}")
    public ResponseEntity<ResponseDTO> rescheduleLesson(@PathVariable String lessonId, @RequestBody LessonDTO lessonDTO)
    {
        ResponseDTO response =
                lessonService.requestReschedule(lessonId, lessonDTO);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PutMapping("/cancelLesson/{lessonId}")
    public ResponseEntity<ResponseDTO> cancelLesson(@PathVariable String lessonId) {

        ResponseDTO response = lessonService.cancelLesson(lessonId);

        HttpStatus status = response.getCode().equals(VarList.RSP_SUCCESS)
                ? HttpStatus.ACCEPTED
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }
    @GetMapping("/student/{id}/upcoming")
    public ResponseEntity<ResponseDTO> getUpcoming(@PathVariable String id) {
        return ResponseEntity.ok(lessonService.getUpcomingLessons(id));
    }

    @GetMapping("/student/{id}/history")
    public ResponseEntity<ResponseDTO> getHistory(@PathVariable String id) {
        return ResponseEntity.ok(lessonService.getLessonHistory(id));
    }

    @PutMapping("/updateStatus/{lessonId}")
    public ResponseEntity updateStatus(@PathVariable String lessonId, @RequestParam LessonStatus status) {

        String response = lessonService.updateLessonStatus(lessonId, status);

        if (response.equals(VarList.RSP_SUCCESS)) {
            return ResponseEntity.ok("Status updated successfully");
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
