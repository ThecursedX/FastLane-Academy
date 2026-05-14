package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.FeedbackDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Feedback;
import com.example.FastLane.Academy.service.FeedbackService;
import com.example.FastLane.Academy.util.SessionUtil;
import com.example.FastLane.Academy.util.VarList;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin
public class FeedbackController {

   @Autowired
   private FeedbackService feedbackService;

    @PostMapping("/add")
    public ResponseEntity<ResponseDTO> addFeedback(@RequestBody FeedbackDTO feedbackDTO, HttpSession session) {
        if (!SessionUtil.isRole(session, "STUDENT")) {

            return ResponseEntity.status(403).body(new ResponseDTO(VarList.UNAUTHORIZED, "Student access only", null));
        }
        String studentId =
                session.getAttribute("userId").toString();

        ResponseDTO response =
                feedbackService.createFeedback(feedbackDTO, studentId);

        HttpStatus status =
                response.getCode().equals(VarList.RSP_SUCCESS)
                        ? HttpStatus.ACCEPTED
                        : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .body(response);    }

    @GetMapping(value ="/getAll")
    public ResponseEntity<ResponseDTO> getAll() {
        List<FeedbackDTO> feedbackList = feedbackService.getAllFeedback();

        return ResponseEntity.ok(new ResponseDTO(VarList.RSP_SUCCESS, "Success", feedbackList));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDTO> update(@RequestBody FeedbackDTO dto) {
        return ResponseEntity.ok(
                feedbackService.updateFeedback(dto)
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDTO> delete(@PathVariable String id,HttpSession session) {
        if (!SessionUtil.isRole(session, "ADMIN")) {

            return ResponseEntity.status(403).body(new ResponseDTO(VarList.UNAUTHORIZED, "Student access only", null));
        }
        return ResponseEntity.ok(
                feedbackService.deleteFeedback(id)
        );
    }
}