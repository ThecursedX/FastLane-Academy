package com.example.FastLane.Academy.repo;

import com.example.FastLane.Academy.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepo
        extends JpaRepository<Admin, String> {

    Optional<Admin> findByEmail(String email);
}
