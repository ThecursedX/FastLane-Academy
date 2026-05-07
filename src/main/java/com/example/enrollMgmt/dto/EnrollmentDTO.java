package com.example.enrollMgmt.dto;

import com.example.enrollMgmt.entity.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentDTO {

    private Long enrollmentId;
    private Long studentId;
    private Long courseId;
    private LocalDate enrolledDate;
    private EnrollmentStatus status;
}