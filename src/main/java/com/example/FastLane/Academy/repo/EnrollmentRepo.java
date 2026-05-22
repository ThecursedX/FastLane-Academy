package com.example.FastLane.Academy.repo;

import com.example.FastLane.Academy.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepo extends JpaRepository<Enrollment, String> {

    Optional<Enrollment> findTopByOrderByEnrollmentIdDesc();

    boolean existsByStudentIdAndCourseId(String studentId, String courseId);

    List<Enrollment> findByStudentId(String studentId);

    List<Enrollment> findByCourseId(String courseId);

    // Used by LessonService to verify student has approved access before claiming a slot
    boolean existsByStudentIdAndCourseIdAndAccessGrantedTrue(String studentId, String courseId);

    // Used by PaymentService to sync paymentStatus back onto enrollment
    Optional<Enrollment> findByStudentIdAndCourseId(String studentId, String courseId);
}