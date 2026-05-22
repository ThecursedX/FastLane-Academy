package com.example.FastLane.Academy.dto;

import com.example.FastLane.Academy.enums.LessonStatus;
import com.example.FastLane.Academy.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonRequestDTO {
    private String requestId;
    private String slotId;
    private String studentId;
    private String courseId;
    private String instructorId;
    private LocalDate date;
    private LocalDateTime requestedAt;
    private RequestStatus status;
    private String lessonId;        // non-null once SELECTED
    private LessonStatus lessonStatus; // populated when lessonId is present
}