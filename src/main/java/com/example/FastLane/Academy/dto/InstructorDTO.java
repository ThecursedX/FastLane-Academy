package com.example.FastLane.Academy.dto;

import com.example.FastLane.Academy.enums.InstructorStatus;
import com.example.FastLane.Academy.enums.WorkingDay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Pattern;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstructorDTO {

    private String instructorId;
    private String instructorName;
    private String email;
    private String licenseId;
    @Pattern(regexp = "^\\d{10}$", message = "Contact number must contain exactly 10 digits")
    private String contactNumber;
    private int experienceYears;
    private String vehicleType;
    private InstructorStatus status;
    private List<WorkingDay> workingDays;
}