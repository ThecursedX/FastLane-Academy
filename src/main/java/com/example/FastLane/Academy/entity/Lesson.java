package com.example.FastLane.Academy.entity;

import com.example.FastLane.Academy.enums.LessonStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "Lessons")
public class Lesson {

        @Id
        private String lessonId;

        private String courseId;
        private String instructorId;
        private String studentId;      // the FIFO-selected student
        private String requestId;      // back-reference to LessonRequest

        private LocalDate date;
        private LocalTime time;

        @Enumerated(EnumType.STRING)
        private LessonStatus status;   // SCHEDULED → COMPLETED / CANCELLED
}