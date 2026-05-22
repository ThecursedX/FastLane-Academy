package com.example.FastLane.Academy.repo;

import com.example.FastLane.Academy.entity.Lesson;
import com.example.FastLane.Academy.enums.LessonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepo extends JpaRepository<Lesson, String> {

    // Used by processNextLesson (FIFO queue)
    Optional<Lesson> findFirstByStatusOrderByLessonIdAsc(LessonStatus status);

    // Used by generateNextId()
    Optional<Lesson> findTopByOrderByLessonIdDesc();

    // Used by getLessonsByStudentId / getUpcomingLessons / getLessonHistory
    List<Lesson> findByStudentId(String studentId);
    List<Lesson> findByInstructorId(String instructorId);
    List<Lesson> findByStudentIdAndStatusAndDateGreaterThanEqual(
            String studentId, LessonStatus status, LocalDate date);
    List<Lesson> findByStudentIdAndDateBefore(String studentId, LocalDate date);

    // Used by getAvailableTimeSlots
    List<Lesson> findByDateAndStatus(LocalDate date, LessonStatus status);

    // Used by InstructorService — check if instructor has future lessons before deactivation
    boolean existsByInstructorIdAndDateGreaterThanEqualAndStatus(
            String instructorId, LocalDate date, LessonStatus status);

    // Used by checkConflict (new booking)
    boolean existsByInstructorIdAndDateAndTimeAndStatus(
            String instructorId, LocalDate date, LocalTime time, LessonStatus status);
    boolean existsByStudentIdAndDateAndTimeAndStatus(
            String studentId, LocalDate date, LocalTime time, LessonStatus status);

    // Used by updateLesson (exclude current lesson from conflict check)
    boolean existsByInstructorIdAndDateAndTimeAndStatusAndLessonIdNot(
            String instructorId, LocalDate date, LocalTime time,
            LessonStatus status, String lessonId);
    boolean existsByStudentIdAndDateAndTimeAndStatusAndLessonIdNot(
            String studentId, LocalDate date, LocalTime time,
            LessonStatus status, String lessonId);
}