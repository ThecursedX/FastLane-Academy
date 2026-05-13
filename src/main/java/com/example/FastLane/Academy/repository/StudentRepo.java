package com.example.FastLane.Academy.repository;

import com.example.FastLane.Academy.entity.Student;
import com.example.FastLane.Academy.entity.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepo extends JpaRepository<Student, String> {

    Optional<Student> findTopByOrderByStudentIdDesc();

    boolean existsByNic(String nic);

    boolean existsByEmail(String email);

    Optional<Student> findByEmail(String email);

    List<Student> findByStatus(StudentStatus status);
}