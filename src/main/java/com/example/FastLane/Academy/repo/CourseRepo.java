package com.example.CourseMgmt.repo;

import com.example.CourseMgmt.entity.Course;
import com.example.CourseMgmt.entity.CourseStatus;
import com.example.CourseMgmt.entity.DifficultyLevel;
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