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
}