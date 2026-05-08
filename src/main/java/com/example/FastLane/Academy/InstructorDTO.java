package com.example.InstructorMgmt.dto;

import com.example.InstructorMgmt.entity.InstructorStatus;
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