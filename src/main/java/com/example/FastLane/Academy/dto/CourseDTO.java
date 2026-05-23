package com.example.FastLane.Academy.dto;

import com.example.FastLane.Academy.enums.CourseStatus;
import com.example.FastLane.Academy.enums.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDTO {

    private String courseId;
    private String courseTitle;
    private String description;
    private int durationHours;
    private java.math.BigDecimal price;
    private DifficultyLevel difficultyLevel;
    private String syllabus;
    private String contentStructure;
    private LocalDateTime lastUpdated;
    private CourseStatus status;
}
