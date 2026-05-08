package com.example.FastLane.Academy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course {

    @Id
    private String courseId;

    @Column(unique = true)
    private String courseTitle;

    private String description;

    private int durationHours;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Column(length = 5000)
    private String syllabus;

    @Column(length = 5000)
    private String contentStructure;

    private LocalDateTime lastUpdated;

    @Enumerated(EnumType.STRING)
    private CourseStatus status;
}