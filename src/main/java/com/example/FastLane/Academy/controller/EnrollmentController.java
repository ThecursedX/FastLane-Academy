package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.EnrollmentDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.service.AdminApprovalService;
import com.example.FastLane.Academy.service.EnrollmentService;
import com.example.FastLane.Academy.util.SessionUtil;
import com.example.FastLane.Academy.util.VarList;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired private EnrollmentService enrollmentService;
    @Autowired private AdminApprovalService adminApprovalService;

    // ── STUDENT: enroll in a course ───────────────────────────────────────
    // Body: { courseId }
    // studentId is injected from the session
    @PostMapping("/enroll")
    public ResponseEntity<ResponseDTO> enrollStudent(
            @RequestBody EnrollmentDTO enrollmentDTO, HttpSession session) {

        if (!SessionUtil.isRole(session, "STUDENT"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Student access only", null));

        // inject from session
        enrollmentDTO.setStudentId((String) session.getAttribute("userId"));

        ResponseDTO response = enrollmentService.enrollStudent(enrollmentDTO);
        HttpStatus status = response.getCode().equals(VarList.RSP_SUCCESS)
                ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    // ── STUDENT: view their own enrollments ───────────────────────────────
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ResponseDTO> getCoursesByStudent(
            @PathVariable String studentId, HttpSession session) {

        if (!SessionUtil.isRole(session, "STUDENT") && !SessionUtil.isRole(session, "ADMIN"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));

        return ResponseEntity.accepted().body(enrollmentService.getCoursesByStudent(studentId));
    }

    // ── ADMIN: view all enrollments for a course (with paymentStatus) ─────
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ResponseDTO> getStudentsByCourse(
            @PathVariable String courseId, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));

        return ResponseEntity.accepted().body(enrollmentService.getStudentsByCourse(courseId));
    }

    // ── ADMIN: approve enrollment (payment-based approval via PaymentController,
    //           this is the manual override path)
    @PutMapping("/approve/{enrollmentId}")
    public ResponseEntity<ResponseDTO> approveEnrollment(
            @PathVariable String enrollmentId, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));

        return ResponseEntity.accepted()
                .body(adminApprovalService.approveEnrollmentById(enrollmentId));
    }

    // ── ADMIN: reject enrollment ──────────────────────────────────────────
    @PutMapping("/reject/{enrollmentId}")
    public ResponseEntity<ResponseDTO> rejectEnrollment(
            @PathVariable String enrollmentId,
            @RequestParam String reason, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));

        return ResponseEntity.accepted()
                .body(adminApprovalService.rejectEnrollmentById(enrollmentId, reason));
    }

    // ── ADMIN: cancel / remove enrollment ────────────────────────────────
    @PutMapping("/remove/{enrollmentId}")
    public ResponseEntity<ResponseDTO> removeEnrollment(
            @PathVariable String enrollmentId, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));

        ResponseDTO response = enrollmentService.removeEnrollment(enrollmentId);
        HttpStatus status = response.getCode().equals(VarList.RSP_SUCCESS)
                ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
}