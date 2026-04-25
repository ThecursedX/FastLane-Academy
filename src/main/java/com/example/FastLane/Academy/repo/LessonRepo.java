package com.example.FastLane.Academy.repo;

import com.example.FastLane.Academy.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;


public interface LessonRepo extends JpaRepository<Lesson, Long> {

    Optional<Lesson> findByInstructorIdAndDateAndTime(
            String instructorId,
            LocalDate date,
            LocalTime time
    );
    Optional<Lesson> findByStudentIdAndDateAndTime(
            String studentId,
            LocalDate date,
            LocalTime time
    );
}
