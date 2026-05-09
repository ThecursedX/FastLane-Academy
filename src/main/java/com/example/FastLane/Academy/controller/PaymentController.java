package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.PaymentDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.enums.PaymentStatus;
import com.example.FastLane.Academy.service.PaymentService;
import com.example.FastLane.Academy.util.VarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Submit Payment.java
    @PostMapping("/submit")
    public ResponseEntity<ResponseDTO> submitPayment(
            @RequestParam String studentId,
            @RequestParam Double amount,
            @RequestParam String paymentMethod,
            @RequestParam String transactionReference,
            @RequestParam(required = false) String lessonId,
            @RequestParam(required = false) String courseId,
            @RequestParam MultipartFile receiptFile
    ) {

        ResponseDTO response =
                paymentService.submitPayment(
                        studentId,
                        amount,
                        paymentMethod,
                        transactionReference,
                        lessonId,
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
                paymentService.approvePayment(paymentId);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    // Reject Payment.java
    @PutMapping("/reject/{paymentId}")
    public ResponseEntity<ResponseDTO> rejectPayment(
            @PathVariable String paymentId,
            @RequestParam String rejectionReason) {

        ResponseDTO response =
                paymentService.rejectPayment(
                        paymentId,
                        rejectionReason
                );

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    // Update Payment.java
    @PutMapping("/update/{paymentId}")
    public ResponseEntity<ResponseDTO> updatePayment(
            @PathVariable String paymentId,
            @RequestBody PaymentDTO paymentDTO) {

        paymentDTO.setPaymentId(paymentId);

        ResponseDTO response =
                paymentService.updatePayment(paymentDTO);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
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
