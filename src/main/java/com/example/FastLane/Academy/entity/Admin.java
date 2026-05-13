package com.example.FastLane.Academy.entity;

import com.example.FastLane.Academy.enums.UserRole;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    private String adminId;

    private String adminName;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;
}