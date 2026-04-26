package com.example.FastLane.Academy.entity;

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
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long lessonId;
        private String studentId;
        private String instructorId;
        private LocalDate date;
        private  LocalTime time;
        private String status ="Scheduled"; // Scheduled, Completed, Cancelled,Pending

        private LocalDateTime requestedAt; // queue order

}
