package com.example.FastLane.Academy.entity;

import com.example.FastLane.Academy.enums.EnrollmentStatus;
import com.example.FastLane.Academy.enums.PaymentStatus;
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
    private EnrollmentStatus status;    // PENDING → APPROVED / REJECTED

    private Boolean accessGranted;      // true once payment is approved

    // Mirrors the linked Payment status so admin sees it without a JOIN
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus; // PENDING / APPROVED / REJECTED / null = not paid yet
}