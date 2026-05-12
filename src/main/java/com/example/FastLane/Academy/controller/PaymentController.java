package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.PaymentDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.enums.PaymentStatus;
import com.example.FastLane.Academy.service.PaymentService;
import com.example.FastLane.Academy.service.AdminApprovalService;
import com.example.FastLane.Academy.util.VarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AdminApprovalService adminApprovalService;

    // Submit Payment.java
    @PostMapping("/submit")
    public ResponseEntity<ResponseDTO> submitPayment(
            @RequestParam String enrollmentId,
            @RequestParam String studentId,
            @RequestParam Double amount,
            @RequestParam String paymentMethod,
            @RequestParam String transactionReference,
            @RequestParam String courseId,
            @RequestParam MultipartFile receiptFile
    ) {

        ResponseDTO response =
                paymentService.submitPayment(
                        enrollmentId,
                        studentId,
                        amount,
                        paymentMethod,
                        transactionReference,
                        courseId,
                        receiptFile
                );

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    // Student Payment.java History
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ResponseDTO> getStudentPayments(
            @PathVariable String studentId) {

        ResponseDTO response =
                paymentService.getStudentPayments(studentId);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    // Admin View All Payments
    @GetMapping("/all")
    public ResponseEntity<ResponseDTO> getAllPayments() {

        ResponseDTO response =
                paymentService.getAllPayments();

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    // Filter By Status
    @GetMapping("/status")
    public ResponseEntity<ResponseDTO> getPaymentsByStatus(
            @RequestParam PaymentStatus status) {

        ResponseDTO response =
                paymentService.getPaymentsByStatus(status);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    // Approve Payment.java
    @PutMapping("/approve/{paymentId}")
    public ResponseEntity<ResponseDTO> approvePayment(
            @PathVariable String paymentId) {

        ResponseDTO response =
                adminApprovalService.approvePayment(paymentId);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    // Reject Payment.java
    @PutMapping("/reject/{paymentId}")
    public ResponseEntity<ResponseDTO> rejectPayment(
            @PathVariable String paymentId,
            @RequestParam String rejectionReason) {

        ResponseDTO response =
                adminApprovalService.rejectPayment(
                        paymentId,
                        rejectionReason
                );

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    // Update Payment.java
    @PutMapping(value = "/update/{paymentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO> updatePayment(
            @PathVariable String paymentId,
            @RequestParam Double amount,
            @RequestParam String paymentMethod,
            @RequestParam String transactionReference,
            @RequestParam MultipartFile receiptFile) {

        ResponseDTO response = paymentService.updatePayment(paymentId, amount, paymentMethod, transactionReference, receiptFile);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    // Delete Payment.java
    @PutMapping("/delete/{paymentId}")
    public ResponseEntity<ResponseDTO> deletePayment(
            @PathVariable String paymentId) {

        ResponseDTO response =
                paymentService.deletePayment(paymentId);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }
}
