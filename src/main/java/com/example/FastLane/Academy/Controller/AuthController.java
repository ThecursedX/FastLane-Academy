package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.LoginDTO;
import com.example.FastLane.Academy.dto.RegisterDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register/student")
    public ResponseEntity<ResponseDTO> registerStudent(
            @RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.ok(
                authService.registerStudent(registerDTO));
    }

    @PostMapping("/register/instructor")
    public ResponseEntity<ResponseDTO> registerInstructor(
            @RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.ok(
                authService.registerInstructor(registerDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(
            @RequestBody LoginDTO dto, HttpSession session) {
        return ResponseEntity.ok(
                authService.login(dto, session));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO> logout(HttpSession session) {
        return ResponseEntity.ok(
                authService.logout(session));
    }
}
