package com.example.FastLane.Academy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Feedback {

    @Id
    private String feedbackId;

    private String studentId;
    private String courseId;   // feedback is on the course
    private String lessonId;   // which lesson triggered this feedback (optional)

    private int rating;        // 1–5
    private String instructorId;  // optional — which instructor this feedback targets
    private String comment;
    private LocalDate feedbackDate;
    private LocalDateTime createdAt;  // used to enforce 48-hour edit window
}