package com.example.FastLane.Academy.util;

import com.example.FastLane.Academy.entity.Admin;
import com.example.FastLane.Academy.enums.UserRole;
import com.example.FastLane.Academy.repo.AdminRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder {

    @Autowired
    private AdminRepo adminRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostConstruct
    public void seedAdmin() {

        boolean exists = adminRepo.existsAdminByEmail("admin@fastlane.com");

        if (!exists) {

            Admin admin = new Admin();

            admin.setAdminId("A001");
            admin.setAdminName("System Admin");
            admin.setEmail("admin@fastlane.com");

            //encoded password
            admin.setPassword(passwordEncoder.encode("admin123"));

            admin.setRole(UserRole.ADMIN);

            adminRepo.save(admin);

            System.out.println("Default admin created");
        }
    }
}
