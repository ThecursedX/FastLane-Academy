package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Enrollment;
import com.example.FastLane.Academy.enums.EnrollmentStatus;
import com.example.FastLane.Academy.entity.Payment;
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

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    public ResponseDTO approvePayment(String paymentId) {

        // Find payment
        Optional<Payment> optionalPayment =
                paymentRepo.findById(paymentId);

        if(optionalPayment.isEmpty()){

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND,
                    "Payment not found",
                    null
            );
        }

        Payment payment = optionalPayment.get();

        // Validate payment status
        if(payment.getStatus() != PaymentStatus.PENDING){

            return new ResponseDTO(
                    VarList.INVALID_PAYMENT_STATUS,
                    "Only pending payments can be approved",
                    payment
            );
        }


        //Approve payment
        payment.setStatus(PaymentStatus.APPROVED);
        paymentRepo.save(payment);

        // Find enrollment
        Optional<Enrollment> optionalEnrollment =
                enrollmentRepo.findById(
                        payment.getEnrollmentId());

        if(optionalEnrollment.isEmpty()){

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND,
                    "Enrollment not found",
                    null
            );
        }

        Enrollment enrollment =
                optionalEnrollment.get();

        //Approve enrollment
        enrollment.setStatus(EnrollmentStatus.APPROVED);

        // 5. Grant course access
        enrollment.setAccessGranted(true);

        enrollmentRepo.save(enrollment);

        return new ResponseDTO(
                VarList.RSP_SUCCESS,
                "Payment approved & course access granted",
                enrollment
        );
    }

    public ResponseDTO rejectPayment(String paymentId,String reason) {

        Optional<Payment> optionalPayment =
                paymentRepo.findById(paymentId);

        if(optionalPayment.isEmpty()){

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND,
                    "Payment not found",
                    null
            );
        }

        Payment payment = optionalPayment.get();

        payment.setStatus(PaymentStatus.REJECTED);
        payment.setRejectionReason(reason);
        paymentRepo.save(payment);

        Optional<Enrollment> optionalEnrollment =
                enrollmentRepo.findById(
                        payment.getEnrollmentId());

        if(optionalEnrollment.isPresent()){

            Enrollment enrollment =
                    optionalEnrollment.get();

            enrollment.setStatus(
                    EnrollmentStatus.REJECTED);

            enrollment.setAccessGranted(false);

            enrollmentRepo.save(enrollment);
        }

        return new ResponseDTO(
                VarList.RSP_SUCCESS,
                "Payment rejected",
                payment
        );
    }

    public ResponseDTO approveEnrollmentById(String enrollmentId) {
        Optional<Enrollment> optional = enrollmentRepo.findById(enrollmentId);
        if (optional.isEmpty()) {
            return new ResponseDTO(VarList.RSP_NO_DATA_FOUND, "Enrollment not found", null);
        }
        Enrollment enrollment = optional.get();
        enrollment.setStatus(EnrollmentStatus.APPROVED);
        enrollment.setAccessGranted(true);
        enrollmentRepo.save(enrollment);
        return new ResponseDTO(VarList.RSP_SUCCESS, "Enrollment approved & access granted", enrollment);
    }

    public ResponseDTO rejectEnrollmentById(String enrollmentId, String reason) {
        Optional<Enrollment> optional = enrollmentRepo.findById(enrollmentId);
        if (optional.isEmpty()) {
            return new ResponseDTO(VarList.RSP_NO_DATA_FOUND, "Enrollment not found", null);
        }
        Enrollment enrollment = optional.get();
        enrollment.setStatus(EnrollmentStatus.REJECTED);
        enrollment.setAccessGranted(false);
        enrollmentRepo.save(enrollment);
        return new ResponseDTO(VarList.RSP_SUCCESS, "Enrollment rejected", enrollment);
    }

}
