package com.example.FastLane.Academy.repo;

import com.example.FastLane.Academy.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


public interface LessonRepo extends JpaRepository<Lesson, Long> {

    boolean existsByInstructorIdAndDateAndTime(
            String instructorId,
            LocalDate date,
            LocalTime time
    );
    boolean existsByStudentIdAndDateAndTime(
            String studentId,
            LocalDate date,
            LocalTime time
    );

    List<Lesson> findByStudentId(String studentId);
    List<Lesson> findByInstructorId(String instructorId);
    List<Lesson> findByStatusOrderByRequestedAtAsc(String status);
    Optional<Lesson> findFirstByStatusOrderByRequestedAtAsc(String status);

    // Conflict checks against already scheduled lessons only
    boolean existsByInstructorIdAndDateAndTimeAndStatusAndLessonIdNot(
            String instructorId,
            LocalDate date,
            LocalTime time,
            String status,
            Long lessonId
    );

    boolean existsByStudentIdAndDateAndTimeAndStatusAndLessonIdNot(
            String studentId,
            LocalDate date,
            LocalTime time,
            String status,
            Long lessonId
    );

    boolean existsByInstructorIdAndDateAndTimeAndStatus(
            String instructorId,
            LocalDate date,
            LocalTime time,
            String status
    );
    boolean existsByStudentIdAndDateAndTimeAndStatus(
            String studentId,
            LocalDate date,
            LocalTime time,
            String status
    );

}
