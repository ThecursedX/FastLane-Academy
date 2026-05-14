package com.example.FastLane.Academy.entity;

import com.example.FastLane.Academy.enums.InstructorStatus;
import com.example.FastLane.Academy.enums.UserRole;
import com.example.FastLane.Academy.enums.WorkingDay;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @Enumerated(EnumType.STRING)
    private InstructorStatus status;

    private String password;

    @ElementCollection(targetClass = WorkingDay.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "instructor_working_days")
    @Column(name = "working_day")
    private List<WorkingDay> workingDays;

    @Enumerated(EnumType.STRING)
    private UserRole role;

}