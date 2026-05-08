package com.example.FastLane.Academy.repo;

import com.example.FastLane.Academy.entity.Course;
import com.example.FastLane.Academy.enums.CourseStatus;
import com.example.FastLane.Academy.enums.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepo extends JpaRepository<Course, String > {

    boolean existsByCourseTitle(String courseTitle);

    List<Course> findByStatus(CourseStatus status);

    List<Course> findByDifficultyLevelAndStatus(
            DifficultyLevel difficultyLevel,
            CourseStatus status
    );
    Optional<Course> findTopByOrderByCourseIdDesc();
}