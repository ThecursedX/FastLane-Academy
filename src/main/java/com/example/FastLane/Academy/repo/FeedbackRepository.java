package com.example.FastLane.Academy.repo;

import com.example.FastLane.Academy.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, String> {

    Optional<Feedback> findTopByOrderByFeedbackIdDesc();

    List<Feedback> findByCourseId(String courseId);

    List<Feedback> findByStudentId(String studentId);

    // Guard: student can only leave one feedback per lesson
    List<Feedback> findByInstructorId(String instructorId);

    boolean existsByStudentIdAndLessonId(String studentId, String lessonId);
}