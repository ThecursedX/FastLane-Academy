package com.example.FastLane.Academy.entity;

import com.example.FastLane.Academy.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    private String paymentId;
    private String enrollmentId;
    private String studentId;
    private Double amount;
    private String paymentMethod;

    @Column(unique = true)
    private String transactionReference;

    // Optional relation references
    private String courseId;

    // receipt image path / filename
    private String receiptUrl;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String rejectionReason;
    private LocalDateTime submittedAt;
    private boolean deleted = false;
}
