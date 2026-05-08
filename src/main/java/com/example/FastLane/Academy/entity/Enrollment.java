package com.example.FastLane.Academy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Enrollment {

    @Id
    private String enrollmentId;

    private String studentId;
    private String courseId;
    private LocalDate enrolledDate;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;
}