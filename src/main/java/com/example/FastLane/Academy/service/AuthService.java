package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.LoginDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.repo.AdminRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private InstructorRepo instructorRepo;

    @Autowired
    private AdminRepo adminRepo;

    public ResponseDTO login(LoginDTO dto) {

        // Check student
        Optional<Student> student =
                studentRepo.findByEmail(dto.getEmail());

        if(student.isPresent()){

            if(student.get().getPassword()
                    .equals(dto.getPassword())){

                return new ResponseDTO(
                        VarList.RSP_SUCCESS,
                        "Student login successful",
                        student.get()
                );
            }
        }

        // Check instructor
        Optional<Instructor> instructor =
                instructorRepo.findByEmail(dto.getEmail());

        if(instructor.isPresent()){

            if(instructor.get().getPassword()
                    .equals(dto.getPassword())){

                return new ResponseDTO(
                        VarList.RSP_SUCCESS,
                        "Instructor login successful",
                        instructor.get()
                );
            }
        }

        // Check admin
        Optional<Admin> admin =
                adminRepo.findByEmail(dto.getEmail());

        if(admin.isPresent()){

            if(admin.get().getPassword()
                    .equals(dto.getPassword())){

                return new ResponseDTO(
                        VarList.RSP_SUCCESS,
                        "Admin login successful",
                        admin.get()
                );
            }
        }

        return new ResponseDTO(
                VarList.UNAUTHORIZED,
                "Invalid credentials",
                null
        );
    }
}
