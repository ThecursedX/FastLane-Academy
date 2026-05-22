package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.InstructorSlotDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.service.InstructorSlotService;
import com.example.FastLane.Academy.service.LessonRequestService;
import com.example.FastLane.Academy.util.SessionUtil;
import com.example.FastLane.Academy.util.VarList;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/slots")
public class InstructorSlotController {

    @Autowired private InstructorSlotService slotService;
    @Autowired private LessonRequestService requestService;

    private ResponseEntity<ResponseDTO> forbidden() {
        return ResponseEntity.status(403)
                .body(new ResponseDTO(VarList.UNAUTHORIZED, "Instructor access only", null));
    }

    // ── Create slot ────────────────────────────────────────────────────────
    // Body: { courseId, dayOfWeek, time }
    @PostMapping("/create")
    public ResponseEntity<ResponseDTO> createSlot(
            @RequestBody InstructorSlotDTO dto, HttpSession session) {
        if (!SessionUtil.isRole(session, "INSTRUCTOR")) return forbidden();
        // Prefer session userId; fall back to DTO-supplied instructorId (cross-origin dev)
        String instructorId = SessionUtil.getUserId(session);
        if (instructorId == null || instructorId.isBlank()) instructorId = dto.getInstructorId();
        if (instructorId == null || instructorId.isBlank())
            return ResponseEntity.status(403).body(new ResponseDTO(VarList.UNAUTHORIZED, "Could not resolve instructor identity", null));
        ResponseDTO res = slotService.createSlot(instructorId, dto);
        return ResponseEntity.status(
                res.getCode().equals(VarList.RSP_SUCCESS) ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST
        ).body(res);
    }

    // ── Enable slot → fires FIFO ────────────────────────────────────────────
    @PutMapping("/enable/{slotId}")
    public ResponseEntity<ResponseDTO> enableSlot(
            @PathVariable String slotId, HttpSession session) {
        if (!SessionUtil.isRole(session, "INSTRUCTOR")) return forbidden();
        String instructorId = SessionUtil.getUserId(session);
        ResponseDTO res = slotService.enableSlot(instructorId, slotId);
        return ResponseEntity.status(
                res.getCode().equals(VarList.RSP_SUCCESS) ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST
        ).body(res);
    }

    // ── Disable slot ────────────────────────────────────────────────────────
    @PutMapping("/disable/{slotId}")
    public ResponseEntity<ResponseDTO> disableSlot(
            @PathVariable String slotId, HttpSession session) {
        if (!SessionUtil.isRole(session, "INSTRUCTOR")) return forbidden();
        String instructorId = SessionUtil.getUserId(session);
        ResponseDTO res = slotService.disableSlot(instructorId, slotId);
        return ResponseEntity.status(
                res.getCode().equals(VarList.RSP_SUCCESS) ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST
        ).body(res);
    }

    // ── Delete slot ─────────────────────────────────────────────────────────
    @DeleteMapping("/delete/{slotId}")
    public ResponseEntity<ResponseDTO> deleteSlot(
            @PathVariable String slotId, HttpSession session) {
        if (!SessionUtil.isRole(session, "INSTRUCTOR")) return forbidden();
        String instructorId = SessionUtil.getUserId(session);
        ResponseDTO res = slotService.deleteSlot(instructorId, slotId);
        return ResponseEntity.status(
                res.getCode().equals(VarList.RSP_SUCCESS) ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST
        ).body(res);
    }

    // ── My slots ────────────────────────────────────────────────────────────
    @GetMapping("/mySlots")
    public ResponseEntity<ResponseDTO> getMySlots(
            @RequestParam(required = false) String instructorId,
            HttpSession session) {
        // Primary: session-based auth (same-origin or cookie properly sent)
        if (SessionUtil.isRole(session, "INSTRUCTOR")) {
            String sessionId = SessionUtil.getUserId(session);
            return ResponseEntity.accepted().body(slotService.getMySlots(sessionId));
        }
        // Fallback: instructorId passed as query param (cross-origin dev where cookie is lost)
        if (instructorId != null && !instructorId.isBlank()) {
            return ResponseEntity.accepted().body(slotService.getMySlots(instructorId));
        }
        return forbidden();
    }


    // ── Student: browse enabled slots available for a course ───────────────
    // Open to STUDENT or ADMIN via session; also accessible with studentId query param (cross-origin dev)
    @GetMapping("/available/{courseId}")
    public ResponseEntity<ResponseDTO> getAvailableForCourse(
            @PathVariable String courseId,
            @RequestParam(required = false) String studentId,
            HttpSession session) {
        boolean hasSession = SessionUtil.isRole(session, "STUDENT") || SessionUtil.isRole(session, "ADMIN");
        boolean hasParam   = studentId != null && !studentId.isBlank();
        if (!hasSession && !hasParam)
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));
        return ResponseEntity.accepted().body(slotService.getAvailableSlotsForCourse(courseId));
    }

    // ── Student: browse slots sorted by instructor experience (bubble sort) ─
    // Returns all slots for a course, sorted descending by instructor experienceYears.
    @GetMapping("/available/{courseId}/sorted-by-experience")
    public ResponseEntity<ResponseDTO> getSlotsSortedByExperience(
            @PathVariable String courseId,
            @RequestParam(required = false) String studentId,
            HttpSession session) {
        boolean hasSession = SessionUtil.isRole(session, "STUDENT") || SessionUtil.isRole(session, "ADMIN");
        boolean hasParam   = studentId != null && !studentId.isBlank();
        if (!hasSession && !hasParam)
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));
        return ResponseEntity.accepted().body(slotService.getSlotsSortedByExperience(courseId));
    }

    // ── Requests for a slot (instructor view) ─────────────────────────────
    @GetMapping("/{slotId}/requests")
    public ResponseEntity<ResponseDTO> getSlotRequests(
            @PathVariable String slotId, HttpSession session) {
        if (!SessionUtil.isRole(session, "INSTRUCTOR") && !SessionUtil.isRole(session, "ADMIN"))
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));
        String instructorId = SessionUtil.getUserId(session);
        return ResponseEntity.accepted().body(requestService.getRequestsForSlot(instructorId, slotId));
    }
}