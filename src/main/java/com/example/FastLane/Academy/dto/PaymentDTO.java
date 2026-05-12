package com.example.FastLane.Academy.dto;

import com.example.FastLane.Academy.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {

    private String paymentId;
    private String enrollmentId;
    private String studentId;
    private Double amount;
    private String paymentMethod;
    private String transactionReference;
    private String courseId;
    private String receiptUrl;
    private PaymentStatus status;
    private String rejectionReason;
    private LocalDateTime submittedAt;
}
