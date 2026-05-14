package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.EnrollmentDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.service.EnrollmentService;
import com.example.FastLane.Academy.util.SessionUtil;
import com.example.FastLane.Academy.util.VarList;
import com.example.FastLane.Academy.service.AdminApprovalService;
import jakarta.servlet.http.HttpSession;
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

    @Autowired
    private AdminApprovalService adminApprovalService;

    // Enroll Student
    @PostMapping("/enroll")
    public ResponseEntity<ResponseDTO> enrollStudent(@RequestBody EnrollmentDTO enrollmentDTO, HttpSession session)
    {


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
            @PathVariable String studentId,HttpSession session) {

        ResponseDTO response =
                enrollmentService.getCoursesByStudent(studentId);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    // View Students By Course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ResponseDTO> getStudentsByCourse(
            @PathVariable String courseId, HttpSession session) {
        if (!SessionUtil.isRole(session, "ADMIN")) {
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));
        }
        ResponseDTO response =
                enrollmentService.getStudentsByCourse(courseId);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    // Remove Enrollment
    @PutMapping("/remove/{enrollmentId}")
    public ResponseEntity<ResponseDTO> removeEnrollment(
            @PathVariable String enrollmentId,HttpSession session)
    {
        if (!SessionUtil.isRole(session, "ADMIN")) {
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));
        }

        ResponseDTO response =
                enrollmentService.removeEnrollment(enrollmentId);

        HttpStatus status =
                response.getCode().equals(VarList.RSP_SUCCESS)
                        ? HttpStatus.ACCEPTED
                        : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }



    // Admin: approve enrollment access manually (edge case) direct access
    @PutMapping("/approve/{enrollmentId}")
    public ResponseEntity<ResponseDTO> approveEnrollment(@PathVariable String enrollmentId,HttpSession session)
    {
        if (!SessionUtil.isRole(session, "ADMIN")) {
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));
        }
        ResponseDTO response = adminApprovalService.approveEnrollmentById(enrollmentId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    // Admin: reject enrollment
    @PutMapping("/reject/{enrollmentId}")
    public ResponseEntity<ResponseDTO> rejectEnrollment(
            @PathVariable String enrollmentId,
            @RequestParam String reason, HttpSession session)
    {
        if (!SessionUtil.isRole(session, "ADMIN")) {
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));
        }
        ResponseDTO response = adminApprovalService.rejectEnrollmentById(enrollmentId, reason);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}