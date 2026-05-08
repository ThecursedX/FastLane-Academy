package com.example.FastLane.Academy.entity;

import com.example.FastLane.Academy.enums.LessonStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "Lessons")
public class Lesson {
        @Id
        private String lessonId;
        private String studentId;
        private String instructorId;
        private LocalDate date;
        private  LocalTime time;

        @Enumerated(EnumType.STRING)
        private LessonStatus status;

        private LocalDateTime requestedAt; // queue order


}
