package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.PaymentDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Payment;
import com.example.FastLane.Academy.enums.PaymentStatus;
import com.example.FastLane.Academy.repo.PaymentRepo;
import com.example.FastLane.Academy.util.VarList;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private ModelMapper modelMapper;

    // Submit Payment.java
    public ResponseDTO submitPayment( String studentId,
                                      Double amount,
                                      String paymentMethod,
                                      String transactionReference,
                                      String lessonId,
                                      String courseId,
                                      MultipartFile receiptFile) {

        try {

            // duplicate transaction validation
            if (paymentRepo.existsByTransactionReference(
                    transactionReference)) {

                return new ResponseDTO(
                        VarList.DUPLICATE_TRANSACTION,
                        "Duplicate transaction reference",
                        null
                );
            }

            // file name generation
            String fileName =
                    System.currentTimeMillis()
                            + "_"
                            + receiptFile.getOriginalFilename();

            // upload folder
            String uploadDir = "uploads/";

            File directory = new File(uploadDir);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            // save file
            Path filePath =
                    Paths.get(uploadDir, fileName);

            Files.copy(
                    receiptFile.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // save payment
            Payment payment = new Payment();

            payment.setStudentId(studentId);
            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod);
            payment.setTransactionReference(transactionReference);
            payment.setLessonId(lessonId);
            payment.setCourseId(courseId);

            // saved image path
            payment.setReceiptUrl(fileName);

            payment.setStatus(PaymentStatus.PENDING);

            payment.setSubmittedAt(LocalDateTime.now());

            //create ID
            String lastId = paymentRepo.findTopByOrderByPaymentIdDesc()
                    .map(Payment::getPaymentId)
                    .orElse(null);

            String nextId;

            if (lastId == null) {
                nextId = "P001";
            } else {

                int number = Integer.parseInt(lastId.substring(1));
                nextId = String.format("P%03d", number + 1);
            }

            payment.setPaymentId(nextId);

            //save
            paymentRepo.save(payment);

            return new ResponseDTO(
                    VarList.RSP_SUCCESS,
                    "Payment.java submitted successfully",
                    payment
            );

        } catch (Exception ex) {

            return new ResponseDTO(
                    VarList.RSP_ERROR,
                    ex.getMessage(),
                    null
            );
        }
    }

    // Student Payment.java History
    public ResponseDTO getStudentPayments(String studentId) {

        List<PaymentDTO> list =
                paymentRepo.findByStudentId(studentId)
                        .stream()
                        .filter(payment -> !payment.isDeleted())
                        .map(payment ->
                                modelMapper.map(payment,
                                        PaymentDTO.class))
                        .toList();

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Payment.java history retrieved successfully", list);
    }

    // Admin View All Payments
    public ResponseDTO getAllPayments() {

        List<PaymentDTO> list =
                paymentRepo.findByDeletedFalse()
                        .stream()
                        .map(payment ->
                                modelMapper.map(payment,
                                        PaymentDTO.class))
                        .toList();

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Payments retrieved successfully", list);
    }

    // Filter Payments By Status
    public ResponseDTO getPaymentsByStatus(PaymentStatus status) {

        List<PaymentDTO> list =
                paymentRepo.findByStatus(status)
                        .stream()
                        .filter(payment -> !payment.isDeleted())
                        .map(payment ->
                                modelMapper.map(payment,
                                        PaymentDTO.class))
                        .toList();

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Filtered payments retrieved successfully", list);
    }

    // Approve Payment.java
    public ResponseDTO approvePayment(String paymentId) {

        Optional<Payment> optionalPayment = paymentRepo.findById(paymentId);

        if (optionalPayment.isEmpty()) {

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND, "Payment.java not found", null);
        }

        Payment payment = optionalPayment.get();

        if (payment.getStatus() != PaymentStatus.PENDING) {

            return new ResponseDTO(
                    VarList.INVALID_PAYMENT_STATUS, "Only pending payments can be approved", payment);
        }

        payment.setStatus(PaymentStatus.APPROVED);

        paymentRepo.save(payment);

        return new ResponseDTO(
                VarList.RSP_SUCCESS,"Payment.java approved successfully", payment);
    }

    // Reject Payment.java
    public ResponseDTO rejectPayment(String paymentId, String rejectionReason) {

        Optional<Payment> optionalPayment =
                paymentRepo.findById(paymentId);

        if (optionalPayment.isEmpty()) {

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND,
                    "Payment.java not found",
                    null
            );
        }

        Payment payment = optionalPayment.get();

        if (payment.getStatus() != PaymentStatus.PENDING) {

            return new ResponseDTO(
                    VarList.INVALID_PAYMENT_STATUS, "Only pending payments can be rejected", payment);
        }

        payment.setStatus(PaymentStatus.REJECTED);

        payment.setRejectionReason(rejectionReason);

        paymentRepo.save(payment);

        return new ResponseDTO(
                VarList.RSP_SUCCESS,
                "Payment.java rejected successfully",
                payment
        );
    }

    // Update Rejected Payment.java
    public ResponseDTO updatePayment(PaymentDTO paymentDTO) {

        Optional<Payment> optionalPayment = paymentRepo.findById(paymentDTO.getPaymentId());

        if (optionalPayment.isEmpty()) {

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND, "Payment.java not found", null);
        }

        Payment payment = optionalPayment.get();

        // Only rejected payments editable
        if (payment.getStatus() != PaymentStatus.REJECTED) {

            return new ResponseDTO(
                    VarList.PAYMENT_UPDATE_NOT_ALLOWED, "Only rejected payments can be updated", payment);
        }

        // Duplicate transaction validation
        boolean duplicate = paymentRepo.existsByTransactionReference(paymentDTO.getTransactionReference());

        if (duplicate &&
                !payment.getTransactionReference().equals(
                        paymentDTO.getTransactionReference())) {

            return new ResponseDTO(VarList.DUPLICATE_TRANSACTION, "Duplicate transaction reference", paymentDTO);
        }

        payment.setAmount(paymentDTO.getAmount());

        payment.setPaymentMethod(
                paymentDTO.getPaymentMethod());

        payment.setTransactionReference(
                paymentDTO.getTransactionReference());

        payment.setReceiptUrl(
                paymentDTO.getReceiptUrl());

        payment.setStatus(PaymentStatus.PENDING);

        payment.setRejectionReason(null);

        paymentRepo.save(payment);

        return new ResponseDTO(
                VarList.UPDATED_SUCCESSFULLY,
                "Payment.java updated successfully",
                payment
        );
    }

    // Soft Delete Payment.java
    public ResponseDTO deletePayment(String paymentId) {

        Optional<Payment> optionalPayment =
                paymentRepo.findById(paymentId);

        if (optionalPayment.isEmpty()) {

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND,
                    "Payment.java not found",
                    null
            );
        }

        Payment payment = optionalPayment.get();

        payment.setDeleted(true);

        paymentRepo.save(payment);

        return new ResponseDTO(
                VarList.RSP_SUCCESS,
                "Payment.java deleted successfully",
                payment
        );
    }
}
