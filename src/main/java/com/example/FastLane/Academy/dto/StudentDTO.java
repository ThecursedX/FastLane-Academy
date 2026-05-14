package com.example.FastLane.Academy.dto;

import com.example.FastLane.Academy.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StudentDTO {

    private String studentId;
    private String fullName;
    private String nic;
    private String email;
    private String contactNumber;
    private String address;
    private LocalDate dateOfBirth;
    private String emergencyContact;
    private StudentStatus status;
}