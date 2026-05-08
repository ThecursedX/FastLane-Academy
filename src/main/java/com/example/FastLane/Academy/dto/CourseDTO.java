package com.example.CourseMgmt.dto;

import com.example.CourseMgmt.entity.CourseStatus;
import com.example.CourseMgmt.entity.DifficultyLevel;
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
    private DifficultyLevel difficultyLevel;
    private String syllabus;
    private String contentStructure;
    private LocalDateTime lastUpdated;
    private CourseStatus status;
}
