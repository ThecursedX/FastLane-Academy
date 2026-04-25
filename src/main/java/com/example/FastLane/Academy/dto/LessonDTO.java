package com.example.FastLane.Academy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LessonDTO {
    private Long lessonId;
    private String studentId;
    private String instructorId;
    private LocalDate date;
    private LocalTime time;
    private String status ="Scheduled"; // Scheduled, Completed, Cancelled
}
