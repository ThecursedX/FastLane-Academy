package com.example.FastLane.Academy.dto;

import com.example.FastLane.Academy.enums.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentDTO {

    private String enrollmentId;
    private String studentId;
    private String courseId;
    private LocalDate enrolledDate;
    private EnrollmentStatus status;
    private Boolean accessGranted;
}