package com.example.enrollMgmt.entity;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long enrollmentId;

    private Long studentId;
    private Long courseId;
    private LocalDate enrolledDate;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;
}