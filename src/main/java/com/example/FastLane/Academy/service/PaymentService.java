package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.PaymentDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Enrollment;
import com.example.FastLane.Academy.entity.Payment;
import com.example.FastLane.Academy.enums.PaymentStatus;
import com.example.FastLane.Academy.repo.EnrollmentRepo;
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

    @Autowired private PaymentRepo paymentRepo;
    @Autowired private EnrollmentRepo enrollmentRepo;
    @Autowired private ModelMapper modelMapper;

    // Student submits payment receipt for their enrollment
    // After submission the enrollment paymentStatus is set to PENDING
    // so admin sees it immediately without waiting for approval
    public ResponseDTO submitPayment(String enrollmentId,
                                     String studentId,
                                     Double amount,
                                     String paymentMethod,
                                     String transactionReference,
                                     String courseId,
                                     MultipartFile receiptFile) {
        try {
            if (paymentRepo.existsByTransactionReference(transactionReference))
                return new ResponseDTO(VarList.DUPLICATE_TRANSACTION,
                        "Duplicate transaction reference", null);

            String fileName = System.currentTimeMillis() + "_" + receiptFile.getOriginalFilename();
            String uploadDir = "uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(receiptFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Payment payment = new Payment();
            payment.setPaymentId(nextPaymentId());
            payment.setEnrollmentId(enrollmentId);
            payment.setStudentId(studentId);
            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod);
            payment.setTransactionReference(transactionReference);
            payment.setCourseId(courseId);
            payment.setReceiptUrl(fileName);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setSubmittedAt(LocalDateTime.now());
            paymentRepo.save(payment);

            // immediately reflect on the enrollment so admin sees "PENDING" payment status
            Optional<Enrollment> optEnrollment = enrollmentRepo.findById(enrollmentId);
            optEnrollment.ifPresent(e -> {
                e.setPaymentStatus(PaymentStatus.PENDING);
                enrollmentRepo.save(e);
            });

            return new ResponseDTO(VarList.RSP_SUCCESS, "Payment submitted — awaiting admin approval", payment);

        } catch (Exception ex) {
            return new ResponseDTO(VarList.RSP_ERROR, ex.getMessage(), null);
        }
    }

    public ResponseDTO getStudentPayments(String studentId) {
        List<PaymentDTO> list = paymentRepo.findByStudentId(studentId).stream()
                .filter(p -> !p.isDeleted())
                .map(p -> modelMapper.map(p, PaymentDTO.class))
                .toList();
        return new ResponseDTO(VarList.RSP_SUCCESS, "Payment history retrieved", list);
    }

    public ResponseDTO getAllPayments() {
        List<PaymentDTO> list = paymentRepo.findByDeletedFalse().stream()
                .map(p -> modelMapper.map(p, PaymentDTO.class))
                .toList();
        return new ResponseDTO(VarList.RSP_SUCCESS, "Payments retrieved", list);
    }

    public ResponseDTO getPaymentsByStatus(PaymentStatus status) {
        List<PaymentDTO> list = paymentRepo.findByStatus(status).stream()
                .filter(p -> !p.isDeleted())
                .map(p -> modelMapper.map(p, PaymentDTO.class))
                .toList();
        return new ResponseDTO(VarList.RSP_SUCCESS, "Filtered payments retrieved", list);
    }

    // Student can resubmit a rejected payment with a new receipt
    public ResponseDTO updatePayment(String paymentId, Double amount, String paymentMethod,
                                     String transactionReference, MultipartFile receiptFile) {
        try {
            Optional<Payment> opt = paymentRepo.findById(paymentId);
            if (opt.isEmpty())
                return new ResponseDTO(VarList.RSP_NO_DATA_FOUND, "Payment not found", null);

            Payment payment = opt.get();

            if (payment.getStatus() != PaymentStatus.REJECTED &&
                    payment.getStatus() != PaymentStatus.PENDING)
                return new ResponseDTO(VarList.PAYMENT_UPDATE_NOT_ALLOWED,
                        "Only pending or rejected payments can be updated", payment);

            boolean duplicate = paymentRepo.existsByTransactionReference(transactionReference);
            if (duplicate && !payment.getTransactionReference().equals(transactionReference))
                return new ResponseDTO(VarList.DUPLICATE_TRANSACTION, "Duplicate transaction reference", null);

            String fileName = System.currentTimeMillis() + "_" + receiptFile.getOriginalFilename();
            String uploadDir = "uploads/";
            new File(uploadDir).mkdirs();
            Files.copy(receiptFile.getInputStream(), Paths.get(uploadDir, fileName),
                    StandardCopyOption.REPLACE_EXISTING);

            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod);
            payment.setTransactionReference(transactionReference);
            payment.setReceiptUrl(fileName);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setRejectionReason(null);
            paymentRepo.save(payment);

            // sync back to enrollment
            Optional<Enrollment> optEnrollment = enrollmentRepo.findById(payment.getEnrollmentId());
            optEnrollment.ifPresent(e -> {
                e.setPaymentStatus(PaymentStatus.PENDING);
                enrollmentRepo.save(e);
            });

            return new ResponseDTO(VarList.UPDATED_SUCCESSFULLY, "Payment updated successfully", payment);

        } catch (Exception ex) {
            return new ResponseDTO(VarList.RSP_ERROR, ex.getMessage(), null);
        }
    }

    public ResponseDTO deletePayment(String paymentId) {
        Optional<Payment> opt = paymentRepo.findById(paymentId);
        if (opt.isEmpty())
            return new ResponseDTO(VarList.RSP_NO_DATA_FOUND, "Payment not found", null);
        Payment p = opt.get();
        p.setDeleted(true);
        paymentRepo.save(p);
        return new ResponseDTO(VarList.RSP_SUCCESS, "Payment deleted", p);
    }

    private String nextPaymentId() {
        String last = paymentRepo.findTopByOrderByPaymentIdDesc()
                .map(Payment::getPaymentId).orElse(null);
        if (last == null) return "P001";
        return String.format("P%03d", Integer.parseInt(last.substring(1)) + 1);
    }
}