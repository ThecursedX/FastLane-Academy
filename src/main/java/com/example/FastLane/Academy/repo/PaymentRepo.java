package com.example.FastLane.Academy.repo;

import com.example.FastLane.Academy.entity.Payment;
import com.example.FastLane.Academy.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, String> {

    boolean existsByTransactionReference(String transactionReference);

    List<Payment> findByStudentId(String studentId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByDeletedFalse();
    Optional<Payment> findTopByOrderByPaymentIdDesc();
}
