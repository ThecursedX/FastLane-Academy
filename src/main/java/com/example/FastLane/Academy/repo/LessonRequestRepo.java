package com.example.FastLane.Academy.repo;

import com.example.FastLane.Academy.entity.LessonRequest;
import com.example.FastLane.Academy.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LessonRequestRepo extends JpaRepository<LessonRequest, String> {

    // All requests for a slot — used to check disable safety / bulk ops
    List<LessonRequest> findBySlotId(String slotId);

    // Active requests for a slot (any status), FIFO ordered
    List<LessonRequest> findBySlotIdAndStatusInOrderByRequestedAtAsc(
            String slotId, List<RequestStatus> statuses);

    // Requests for a slot on a specific date — to find FIFO head after cancel/reschedule
    List<LessonRequest> findBySlotIdAndDateAndStatusInOrderByRequestedAtAsc(
            String slotId, LocalDate date, List<RequestStatus> statuses);

    // All requests by a student
    List<LessonRequest> findByStudentId(String studentId);

    // Student's active requests for a specific slot+date (guard duplicate)
    List<LessonRequest> findByStudentIdAndSlotIdAndDateAndStatusIn(
            String studentId, String slotId, LocalDate date, List<RequestStatus> statuses);

    // Used by LessonRequestService: one scheduled lesson per student per day
    // (checks if the student already has a SELECTED request — i.e. a confirmed lesson — on this date)
    boolean existsByStudentIdAndDateAndStatus(
            String studentId, LocalDate date, RequestStatus status);

    // All requests for a slot on a specific date (any status)
    List<LessonRequest> findBySlotIdAndDate(String slotId, LocalDate date);

    // All of a student's queued requests on a given date, excluding one specific request
    // Used to cancel competing same-day queues when a student gets SELECTED
    List<LessonRequest> findByStudentIdAndDateAndStatusInAndRequestIdNot(
            String studentId, LocalDate date, List<RequestStatus> statuses, String excludeRequestId);

    Optional<LessonRequest> findTopByOrderByRequestIdDesc();
}