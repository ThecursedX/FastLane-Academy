package com.example.FastLane.Academy.dto;

import com.example.FastLane.Academy.enums.LessonStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LessonDTO {
    private String lessonId;
    private String studentId;
    private String instructorId;
    private LocalDate date;
    private LocalTime time;

    private LessonStatus status;

    private LocalDateTime requestedAt; // queue order

}
