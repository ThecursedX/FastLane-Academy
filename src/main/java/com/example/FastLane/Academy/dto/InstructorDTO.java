package com.example.FastLane.Academy.dto;

import com.example.FastLane.Academy.enums.InstructorStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstructorDTO {

    private String instructorId;
    private String instructorName;
    private String email;
    private String licenseId;
    private String contactNumber;
    private int experienceYears;
    private String vehicleType;
    private String availabilitySchedule;
    private InstructorStatus status;
}