package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.CourseDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.enums.DifficultyLevel;
import com.example.FastLane.Academy.service.CourseService;
import com.example.FastLane.Academy.util.SessionUtil;
import com.example.FastLane.Academy.util.VarList;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ResponseDTO responseDTO;

    @PostMapping("/addCourse")
    public ResponseEntity<ResponseDTO> addCourse(
            @RequestBody CourseDTO courseDTO, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN")) {
            return ResponseEntity.status(403).body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));
        }

        ResponseDTO response = courseService.addCourse(courseDTO);

        HttpStatus status =
                response.getCode().equals(VarList.RSP_SUCCESS)
                        ? HttpStatus.ACCEPTED
                        : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/getAllCourses")
    public ResponseEntity getAllCourses() {

        try {

            List<CourseDTO> courseDTOList =
                    courseService.getAllCourses();

            responseDTO.setCode(VarList.RSP_SUCCESS);
            responseDTO.setMessage("Success");
            responseDTO.setContent(courseDTOList);

            return new ResponseEntity(
                    responseDTO,
                    HttpStatus.ACCEPTED
            );

        } catch (Exception ex) {

            responseDTO.setCode(VarList.RSP_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);

            return new ResponseEntity(
                    responseDTO,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/getCourse/{courseId}")
    public ResponseEntity<ResponseDTO> getCourseById(
            @PathVariable String courseId) {

        ResponseDTO response = courseService.getCourseById(courseId);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    @PutMapping("/updateCourse/{courseId}")
    public ResponseEntity<ResponseDTO> updateCourse(
            @PathVariable String courseId,
            @RequestBody CourseDTO courseDTO, HttpSession session)
    {
        if (!SessionUtil.isRole(session, "ADMIN")) {
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));
        }

        courseDTO.setCourseId(courseId);

        ResponseDTO response = courseService.updateCourse(courseDTO);

        HttpStatus status =
                response.getCode().equals(
                        VarList.UPDATED_SUCCESSFULLY)
                        ? HttpStatus.ACCEPTED
                        : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }

    @PutMapping("/deleteCourse/{courseId}")
    public ResponseEntity<ResponseDTO> deleteCourse(
            @PathVariable String courseId, HttpSession session)
    {
        if (!SessionUtil.isRole(session, "ADMIN")) {
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));
        }

        ResponseDTO response = courseService.deleteCourse(courseId);

        HttpStatus status =
                response.getCode().equals(VarList.RSP_SUCCESS)
                        ? HttpStatus.ACCEPTED
                        : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/filterByDifficulty")
    public ResponseEntity<ResponseDTO> filterByDifficulty(
            @RequestParam DifficultyLevel difficultyLevel) {

        ResponseDTO response = courseService.getCoursesByDifficulty(difficultyLevel);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }


}