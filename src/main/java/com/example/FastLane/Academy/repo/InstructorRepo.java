package com.example.FastLane.Academy.repo;

import com.example.FastLane.Academy.entity.Instructor;
import com.example.FastLane.Academy.enums.InstructorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorRepo extends JpaRepository<Instructor, String> {

    boolean existsByEmail(String email);

    boolean existsByLicenseId(String licenseId);

    List<Instructor> findByStatus(InstructorStatus status);

    Optional<Instructor> findByInstructorId(Long instructorId);

    //last instructor from DB
    @Query(value =
            "SELECT instructor_id FROM instructor " +
                    "ORDER BY instructor_id DESC LIMIT 1",
            nativeQuery = true)
    String getLastInstructorId();

}