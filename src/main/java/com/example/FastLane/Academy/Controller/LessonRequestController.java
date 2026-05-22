package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.LessonRequestDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.service.LessonRequestService;
import com.example.FastLane.Academy.util.SessionUtil;
import com.example.FastLane.Academy.util.VarList;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
public class LessonRequestController {

    @Autowired private LessonRequestService requestService;

    private ResponseEntity<ResponseDTO> forbidden(String msg) {
        return ResponseEntity.status(403)
                .body(new ResponseDTO(VarList.UNAUTHORIZED, msg, null));
    }

    // ── Student: request a slot for a specific date ────────────────────────
    // Body: { slotId, courseId, date, studentId }
    @PostMapping("/request")
    public ResponseEntity<ResponseDTO> requestSlot(
            @RequestBody LessonRequestDTO dto, HttpSession session) {
        String studentId = SessionUtil.isRole(session, "STUDENT") ? SessionUtil.getUserId(session) : null;
        if (studentId == null || studentId.isBlank()) studentId = dto.getStudentId();
        if (studentId == null || studentId.isBlank()) return forbidden("Student access only");
        ResponseDTO res = requestService.requestSlot(studentId, dto);
        return ResponseEntity.status(
                res.getCode().equals(VarList.REQUEST_ADDED) ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST
        ).body(res);
    }

    // ── Student: reschedule (cancel PENDING/IN_QUEUE request) ─────────────
    @PutMapping("/reschedule/{requestId}")
    public ResponseEntity<ResponseDTO> reschedule(
            @PathVariable String requestId,
            @RequestParam(required = false) String studentId,
            HttpSession session) {
        String sid = SessionUtil.isRole(session, "STUDENT") ? SessionUtil.getUserId(session) : studentId;
        if (sid == null || sid.isBlank()) return forbidden("Student access only");
        ResponseDTO res = requestService.reschedule(sid, requestId);
        return ResponseEntity.status(
                res.getCode().equals(VarList.RSP_SUCCESS) ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST
        ).body(res);
    }

    // ── Student: cancel a confirmed (SELECTED) lesson ──────────────────────
    @PutMapping("/cancel/{requestId}")
    public ResponseEntity<ResponseDTO> cancelLesson(
            @PathVariable String requestId,
            @RequestParam(required = false) String studentId,
            HttpSession session) {
        String sid = SessionUtil.isRole(session, "STUDENT") ? SessionUtil.getUserId(session) : studentId;
        if (sid == null || sid.isBlank()) return forbidden("Student access only");
        ResponseDTO res = requestService.cancelLesson(sid, requestId);
        return ResponseEntity.status(
                res.getCode().equals(VarList.RSP_SUCCESS) ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST
        ).body(res);
    }

    // ── Student: view all my requests ─────────────────────────────────────
    @GetMapping("/myRequests")
    public ResponseEntity<ResponseDTO> myRequests(
            @RequestParam(required = false) String studentId,
            HttpSession session) {
        String sid = SessionUtil.isRole(session, "STUDENT") ? SessionUtil.getUserId(session) : studentId;
        if (sid == null || sid.isBlank()) return forbidden("Student access only");
        return ResponseEntity.accepted().body(requestService.getMyRequests(sid));
    }
}