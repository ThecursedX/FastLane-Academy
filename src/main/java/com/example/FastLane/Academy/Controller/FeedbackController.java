package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.FeedbackDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.service.FeedbackService;
import com.example.FastLane.Academy.util.SessionUtil;
import com.example.FastLane.Academy.util.VarList;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired private FeedbackService feedbackService;

    // ── STUDENT: submit feedback after a completed lesson ─────────────────
    // Body: { courseId, lessonId (optional), rating, comment }
    // studentId is injected from the session
    @PostMapping("/submit")
    public ResponseEntity<ResponseDTO> submitFeedback(
            @RequestBody FeedbackDTO feedbackDTO, HttpSession session) {

        if (!SessionUtil.isRole(session, "STUDENT"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Student access only", null));

        String studentId = (String) session.getAttribute("userId");
        ResponseDTO response = feedbackService.createFeedback(feedbackDTO, studentId);

        HttpStatus status = response.getCode().equals(VarList.RSP_SUCCESS)
                ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    // Alias — used by some frontend pages
    @PostMapping("/add")
    public ResponseEntity<ResponseDTO> addFeedback(
            @RequestBody FeedbackDTO feedbackDTO, HttpSession session) {
        return submitFeedback(feedbackDTO, session);
    }

    // ── READ endpoints ────────────────────────────────────────────────────

    @GetMapping("/public")
    public ResponseEntity<ResponseDTO> getPublicFeedback() {
        return ResponseEntity.ok(
                new ResponseDTO(VarList.RSP_SUCCESS, "Success", feedbackService.getAllFeedback()));
    }

    @GetMapping("/getAll")
    public ResponseEntity<ResponseDTO> getAll(HttpSession session) {
        if (!SessionUtil.isRole(session, "ADMIN"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));
        return ResponseEntity.ok(
                new ResponseDTO(VarList.RSP_SUCCESS, "Success", feedbackService.getAllFeedback()));
    }

    // Feedback for a specific course — visible to admin and enrolled students
    @GetMapping("/byCourse/{courseId}")
    public ResponseEntity<ResponseDTO> getByCourse(
            @PathVariable String courseId, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN")
                && !SessionUtil.isRole(session, "STUDENT")
                && !SessionUtil.isRole(session, "INSTRUCTOR"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));

        return ResponseEntity.ok(
                new ResponseDTO(VarList.RSP_SUCCESS, "Success",
                        feedbackService.getFeedbackByCourse(courseId)));
    }

    @GetMapping("/byStudent/{studentId}")
    public ResponseEntity<ResponseDTO> getByStudent(
            @PathVariable String studentId, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN") && !SessionUtil.isRole(session, "STUDENT"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));

        return ResponseEntity.ok(
                new ResponseDTO(VarList.RSP_SUCCESS, "Success",
                        feedbackService.getFeedbackByStudent(studentId)));
    }


    @GetMapping("/byInstructor/{instructorId}")
    public ResponseEntity<ResponseDTO> getByInstructor(
            @PathVariable String instructorId, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN")
                && !SessionUtil.isRole(session, "INSTRUCTOR"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));

        return ResponseEntity.ok(
                new ResponseDTO(VarList.RSP_SUCCESS, "Success",
                        feedbackService.getFeedbackByInstructor(instructorId)));
    }

    @DeleteMapping("/delete/own/{id}")
    public ResponseEntity<ResponseDTO> deleteOwn(
            @PathVariable String id, HttpSession session) {

        if (!SessionUtil.isRole(session, "STUDENT"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Student access only", null));

        return ResponseEntity.ok(feedbackService.deleteFeedback(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDTO> update(
            @PathVariable String id, @RequestBody FeedbackDTO dto, HttpSession session) {

        if (!SessionUtil.isRole(session, "STUDENT") && !SessionUtil.isRole(session, "ADMIN"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));

        // Students can only edit their own reviews
        if (SessionUtil.isRole(session, "STUDENT")) {
            String sessionUserId = (String) session.getAttribute("userId");
            if (dto.getStudentId() != null && !dto.getStudentId().equals(sessionUserId))
                return ResponseEntity.status(403)
                        .body(new ResponseDTO(VarList.UNAUTHORIZED, "You can only edit your own reviews", null));
        }

        dto.setFeedbackId(id);
        return ResponseEntity.ok(feedbackService.updateFeedback(dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDTO> delete(
            @PathVariable String id, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));

        return ResponseEntity.ok(feedbackService.deleteFeedback(id));
    }
}