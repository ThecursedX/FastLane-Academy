package com.example.FastLane.Academy.dto;

import com.example.FastLane.Academy.enums.WorkingDay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {

    private String fullName;
    private String email;
    private String password;
    private String contactNumber;

    private String role;

    // Instructor-specific
    private String licenseId;
    private Integer experienceYears;
    private String vehicleType;

    private List<WorkingDay> workingDays;

    // Student fields (optional)
    private String nic;
    private String address;
    private LocalDate dateOfBirth;
    private String emergencyContact;
}