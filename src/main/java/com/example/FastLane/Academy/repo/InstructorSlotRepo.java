package com.example.FastLane.Academy.repo;

import com.example.FastLane.Academy.entity.InstructorSlot;
import com.example.FastLane.Academy.enums.WorkingDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface InstructorSlotRepo extends JpaRepository<InstructorSlot, String> {

    List<InstructorSlot> findByInstructorId(String instructorId);

    List<InstructorSlot> findByInstructorIdAndEnabled(String instructorId, boolean enabled);

    Optional<InstructorSlot> findTopByOrderBySlotIdDesc();

    boolean existsByInstructorIdAndCourseIdAndDayOfWeekAndTime(
            String instructorId, String courseId, WorkingDay dayOfWeek, LocalTime time);

    List<InstructorSlot> findByCourseId(String courseId);
}