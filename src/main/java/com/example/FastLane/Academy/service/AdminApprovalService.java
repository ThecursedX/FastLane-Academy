package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Enrollment;
import com.example.FastLane.Academy.entity.Payment;
import com.example.FastLane.Academy.enums.EnrollmentStatus;
import com.example.FastLane.Academy.enums.PaymentStatus;
import com.example.FastLane.Academy.repo.EnrollmentRepo;
import com.example.FastLane.Academy.repo.PaymentRepo;
import com.example.FastLane.Academy.util.VarList;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class AdminApprovalService {

    @Autowired private PaymentRepo paymentRepo;
    @Autowired private EnrollmentRepo enrollmentRepo;

    // ─────────────────────────────────────────────────────────────────────────
    // Admin approves a payment → enrollment gets access + paymentStatus synced
    // ─────────────────────────────────────────────────────────────────────────
    public ResponseDTO approvePayment(String paymentId) {

        Optional<Payment> optPayment = paymentRepo.findById(paymentId);
        if (optPayment.isEmpty())
            return new ResponseDTO(VarList.RSP_NO_DATA_FOUND, "Payment not found", null);

        Payment payment = optPayment.get();

        if (payment.getStatus() != PaymentStatus.PENDING)
            return new ResponseDTO(VarList.INVALID_PAYMENT_STATUS,
                    "Only pending payments can be approved", payment);

        payment.setStatus(PaymentStatus.APPROVED);
        paymentRepo.save(payment);

        // sync onto the enrollment so admin sees it in one place
        Optional<Enrollment> optEnrollment = enrollmentRepo.findById(payment.getEnrollmentId());
        if (optEnrollment.isEmpty())
            return new ResponseDTO(VarList.RSP_NO_DATA_FOUND, "Enrollment not found", null);

        Enrollment enrollment = optEnrollment.get();
        enrollment.setStatus(EnrollmentStatus.APPROVED);
        enrollment.setAccessGranted(true);
        enrollment.setPaymentStatus(PaymentStatus.APPROVED); // visible on admin enrollment list
        enrollmentRepo.save(enrollment);

        return new ResponseDTO(VarList.RSP_SUCCESS,
                "Payment approved — course access granted", enrollment);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin rejects a payment → enrollment loses access + paymentStatus synced
    // ─────────────────────────────────────────────────────────────────────────
    public ResponseDTO rejectPayment(String paymentId, String reason) {

        Optional<Payment> optPayment = paymentRepo.findById(paymentId);
        if (optPayment.isEmpty())
            return new ResponseDTO(VarList.RSP_NO_DATA_FOUND, "Payment not found", null);

        Payment payment = optPayment.get();
        payment.setStatus(PaymentStatus.REJECTED);
        payment.setRejectionReason(reason);
        paymentRepo.save(payment);

        Optional<Enrollment> optEnrollment = enrollmentRepo.findById(payment.getEnrollmentId());
        if (optEnrollment.isPresent()) {
            Enrollment enrollment = optEnrollment.get();
            enrollment.setStatus(EnrollmentStatus.REJECTED);
            enrollment.setAccessGranted(false);
            enrollment.setPaymentStatus(PaymentStatus.REJECTED);
            enrollmentRepo.save(enrollment);
        }

        return new ResponseDTO(VarList.RSP_SUCCESS, "Payment rejected", payment);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin manually approves enrollment (edge case — no payment required)
    // ─────────────────────────────────────────────────────────────────────────
    public ResponseDTO approveEnrollmentById(String enrollmentId) {

        Optional<Enrollment> optional = enrollmentRepo.findById(enrollmentId);
        if (optional.isEmpty())
            return new ResponseDTO(VarList.RSP_NO_DATA_FOUND, "Enrollment not found", null);

        Enrollment enrollment = optional.get();
        enrollment.setStatus(EnrollmentStatus.APPROVED);
        enrollment.setAccessGranted(true);
        // paymentStatus stays as-is (could be null if no payment was submitted)
        enrollmentRepo.save(enrollment);

        return new ResponseDTO(VarList.RSP_SUCCESS,
                "Enrollment approved — access granted", enrollment);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin manually rejects enrollment
    // ─────────────────────────────────────────────────────────────────────────
    public ResponseDTO rejectEnrollmentById(String enrollmentId, String reason) {

        Optional<Enrollment> optional = enrollmentRepo.findById(enrollmentId);
        if (optional.isEmpty())
            return new ResponseDTO(VarList.RSP_NO_DATA_FOUND, "Enrollment not found", null);

        Enrollment enrollment = optional.get();
        enrollment.setStatus(EnrollmentStatus.REJECTED);
        enrollment.setAccessGranted(false);
        enrollmentRepo.save(enrollment);

        return new ResponseDTO(VarList.RSP_SUCCESS, "Enrollment rejected", enrollment);
    }
}