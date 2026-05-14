package com.example.FastLane.Academy.entity;

import com.example.FastLane.Academy.enums.StudentStatus;
import com.example.FastLane.Academy.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "students")
public class Student {

    @Id
    private String studentId;

    private String fullName;

    @Column(unique = true)
    private String nic;

    @Column(unique = true)
    private String email;

    private String contactNumber;
    private String address;
    private LocalDate dateOfBirth;
    private String emergencyContact;

    @Enumerated(EnumType.STRING)
    private StudentStatus status;

    private LocalDate registeredDate;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;
}