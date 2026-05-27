package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.LessonDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.enums.LessonStatus;
import com.example.FastLane.Academy.service.LessonService;
import com.example.FastLane.Academy.util.SessionUtil;
import com.example.FastLane.Academy.util.VarList;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    @Autowired private LessonService lessonService;



    // ── ADMIN: process next lesson in the queue ───────────────────────────
    @PostMapping("/processNextLesson")
    public ResponseEntity<ResponseDTO> processNextLesson(HttpSession session) {
        if (!SessionUtil.isRole(session, "ADMIN"))
            return ResponseEntity.status(403).body(
                    new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));

        ResponseDTO response = lessonService.processNextLesson();
        HttpStatus status = response.getCode().equals(VarList.LESSON_SCHEDULED_SUCCESSFULLY)
                ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }



    // ── Lessons by student ────────────────────────────────────────────────
    @GetMapping("/getLessonsByStudentId/{studentId}")
    public ResponseEntity<ResponseDTO> getLessonsByStudent(
            @PathVariable String studentId, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN") && !SessionUtil.isRole(session, "STUDENT"))
            return ResponseEntity.status(403).body(
                    new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));

        return ResponseEntity.accepted().body(new ResponseDTO(
                VarList.RSP_SUCCESS, "Success", lessonService.getLessonsByStudentId(studentId)));
    }

    // ── Lessons by instructor ─────────────────────────────────────────────
    @GetMapping("/getLessonsByInstructorId/{instructorId}")
    public ResponseEntity<ResponseDTO> getLessonsByInstructor(
            @PathVariable String instructorId, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN") && !SessionUtil.isRole(session, "INSTRUCTOR"))
            return ResponseEntity.status(403).body(
                    new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));

        return ResponseEntity.accepted().body(new ResponseDTO(
                VarList.RSP_SUCCESS, "Success", lessonService.getLessonsByInstructorId(instructorId)));
    }



    // ── Update status ─────────────────────────────────────────────────────
    @PutMapping("/updateStatus/{lessonId}")
    public ResponseEntity<ResponseDTO> updateStatus(
            @PathVariable String lessonId,
            @RequestParam LessonStatus status,
            HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN") && !SessionUtil.isRole(session, "INSTRUCTOR"))
            return ResponseEntity.status(403).body(
                    new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));

        String instructorId = SessionUtil.getUserId(session);
        String result = lessonService.updateLessonStatus(lessonId, status, instructorId);
        if (result.equals(VarList.RSP_SUCCESS))
            return ResponseEntity.ok(new ResponseDTO(VarList.RSP_SUCCESS, "Status updated", null));
        return ResponseEntity.badRequest().body(new ResponseDTO(result, "Update failed", null));
    }

    // ── STUDENT: request a lesson slot ───────────────────────────────────
   /* @PostMapping("/requestLesson")
    public ResponseEntity<ResponseDTO> requestLesson(
            @RequestBody LessonDTO lessonDTO, HttpSession session) {

        if (!SessionUtil.isRole(session, "STUDENT"))
            return ResponseEntity.status(403).body(
                    new ResponseDTO(VarList.UNAUTHORIZED, "Student access only", null));

        // Inject studentId from session — never trust the client body
        lessonDTO.setStudentId(SessionUtil.getUserId(session));

        ResponseDTO response = lessonService.requestLesson(lessonDTO);
        HttpStatus status = response.getCode().equals(VarList.REQUEST_ADDED)
                ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }


    // ── Student upcoming / history ────────────────────────────────────────
    @GetMapping("/student/{id}/upcoming")
    public ResponseEntity<ResponseDTO> getUpcoming(
            @PathVariable String id, HttpSession session) {

        if (!SessionUtil.isRole(session, "STUDENT") && !SessionUtil.isRole(session, "ADMIN"))
            return ResponseEntity.status(403).body(
                    new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));

        return ResponseEntity.ok(lessonService.getUpcomingLessons(id));
    }

    @GetMapping("/student/{id}/history")
    public ResponseEntity<ResponseDTO> getHistory(
            @PathVariable String id, HttpSession session) {

        if (!SessionUtil.isRole(session, "STUDENT") && !SessionUtil.isRole(session, "ADMIN"))
            return ResponseEntity.status(403).body(
                    new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));

        return ResponseEntity.ok(lessonService.getLessonHistory(id));
    }

    // ── Available time slots ──────────────────────────────────────────────
    @GetMapping("/available-slots")
    public ResponseEntity<ResponseDTO> getAvailableSlots(
            @RequestParam LocalDate date, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN") &&
                !SessionUtil.isRole(session, "STUDENT") &&
                !SessionUtil.isRole(session, "INSTRUCTOR"))
            return ResponseEntity.status(403).body(
                    new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));

        return ResponseEntity.accepted().body(lessonService.getAvailableTimeSlots(date));
    }

    // ── Update lesson ─────────────────────────────────────────────────────
    @PutMapping("/updateLesson/{lessonId}")
    public ResponseEntity<ResponseDTO> updateLesson(
            @PathVariable String lessonId,
            @RequestBody LessonDTO lessonDTO, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN") &&
                !SessionUtil.isRole(session, "STUDENT") &&
                !SessionUtil.isRole(session, "INSTRUCTOR"))
            return ResponseEntity.status(403).body(
                    new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));

        lessonDTO.setLessonId(lessonId);
        ResponseDTO response = lessonService.updateLesson(lessonDTO);
        HttpStatus status = response.getCode().equals(VarList.UPDATED_SUCCESSFULLY)
                ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
    // ── Cancel lesson ─────────────────────────────────────────────────────
   @PutMapping("/cancelLesson/{lessonId}")
    public ResponseEntity<ResponseDTO> cancelLesson(
            @PathVariable String lessonId, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN") &&
                !SessionUtil.isRole(session, "STUDENT") &&
                !SessionUtil.isRole(session, "INSTRUCTOR"))
            return ResponseEntity.status(403).body(
                    new ResponseDTO(VarList.UNAUTHORIZED, "Access denied", null));

        ResponseDTO response = lessonService.cancelLesson(lessonId);
        HttpStatus status = response.getCode().equals(VarList.RSP_SUCCESS)
                ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    // ── Delete lesson ─────────────────────────────────────────────────────
    @DeleteMapping("/deleteLesson/{lessonId}")
    public ResponseEntity<ResponseDTO> deleteLesson(
            @PathVariable String lessonId, HttpSession session) {

        if (!SessionUtil.isRole(session, "ADMIN"))
            return ResponseEntity.status(403).body(
                    new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));

        ResponseDTO response = lessonService.deleteLesson(lessonId);
        HttpStatus status = response.getCode().equals(VarList.RSP_SUCCESS)
                ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    // ── ADMIN: all lessons ────────────────────────────────────────────────
    @GetMapping("/getAllLessons")
    public ResponseEntity<ResponseDTO> getAllLessons(HttpSession session) {
        if (!SessionUtil.isRole(session, "ADMIN"))
            return ResponseEntity.status(403).body(
                    new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));

        List<LessonDTO> list = lessonService.getAllLessons();
        return ResponseEntity.accepted().body(
                new ResponseDTO(VarList.RSP_SUCCESS, "Success", list));
    }

    // ── Single lesson ─────────────────────────────────────────────────────
    @GetMapping("/getLesson/{lessonId}")
    public ResponseEntity<ResponseDTO> getLessonById(@PathVariable String lessonId) {
        return ResponseEntity.accepted().body(lessonService.getLessonById(lessonId));
    }*/
}