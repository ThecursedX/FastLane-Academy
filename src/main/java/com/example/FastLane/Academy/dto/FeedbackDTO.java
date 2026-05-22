package com.example.FastLane.Academy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDTO {

    private String feedbackId;
    private String studentId;
    private String courseId;   // required — feedback is tied to a course
    private String lessonId;
    private String instructorId;   // optional — which specific lesson
    private int rating;        // 1–5
    private String comment;
    private LocalDate feedbackDate;
    private LocalDateTime createdAt;
}