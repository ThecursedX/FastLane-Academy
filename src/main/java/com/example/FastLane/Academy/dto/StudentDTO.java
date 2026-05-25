package com.example.FastLane.Academy.dto;

import com.example.FastLane.Academy.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StudentDTO {

    private String studentId;
    private String fullName;
    private String nic;
    private String email;
    @Pattern(regexp = "^\\d{10}$", message = "Contact number must contain exactly 10 digits")
    private String contactNumber;
    private String address;
    private LocalDate dateOfBirth;
    @Pattern(regexp = "^\\d{10}$", message = "Emergency contact number must contain exactly 10 digits")
    private String emergencyContact;
    private StudentStatus status;
}