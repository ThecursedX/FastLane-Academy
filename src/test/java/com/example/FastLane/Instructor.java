package com.example.InstructorMgmt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Instructor {

    @Id
    private String instructorId;
    private String instructorName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String licenseId;
    private String contactNumber;
    private int experienceYears;
    private String vehicleType;
    private String availabilitySchedule;

    @Enumerated(EnumType.STRING)
    private InstructorStatus status;
}