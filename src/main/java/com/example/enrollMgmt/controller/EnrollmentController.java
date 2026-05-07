package com.example.enrollMgmt.controller;

import com.example.enrollMgmt.dto.EnrollmentDTO;
import com.example.enrollMgmt.dto.ResponseDTO;
import com.example.enrollMgmt.service.EnrollmentService;
import com.example.enrollMgmt.util.VarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    // Enroll Student
    @PostMapping("/enroll")
    public ResponseEntity<ResponseDTO> enrollStudent(@RequestBody EnrollmentDTO enrollmentDTO) {

        ResponseDTO response =
                enrollmentService.enrollStudent(enrollmentDTO);

        HttpStatus status =
                response.getCode().equals(VarList.RSP_SUCCESS)
                        ? HttpStatus.ACCEPTED
                        : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }

    // View Courses By Student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ResponseDTO> getCoursesByStudent(
            @PathVariable Long studentId) {

        ResponseDTO response =
                enrollmentService.getCoursesByStudent(studentId);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    // View Students By Course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ResponseDTO> getStudentsByCourse(
            @PathVariable Long courseId) {

        ResponseDTO response =
                enrollmentService.getStudentsByCourse(courseId);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    // Remove Enrollment
    @PutMapping("/remove/{enrollmentId}")
    public ResponseEntity<ResponseDTO> removeEnrollment(
            @PathVariable Long enrollmentId) {

        ResponseDTO response =
                enrollmentService.removeEnrollment(enrollmentId);

        HttpStatus status =
                response.getCode().equals(VarList.RSP_SUCCESS)
                        ? HttpStatus.ACCEPTED
                        : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }
}