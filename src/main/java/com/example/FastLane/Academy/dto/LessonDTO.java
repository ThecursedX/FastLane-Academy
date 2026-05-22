package com.example.FastLane.Academy.dto;

import com.example.FastLane.Academy.enums.LessonStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonDTO {
    private String lessonId;
    private String courseId;
    private String instructorId;
    private String studentId;
    private String requestId;
    private LocalDate date;
    private LocalTime time;
    private LessonStatus status;
}