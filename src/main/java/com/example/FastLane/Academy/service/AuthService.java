package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.LoginDTO;
import com.example.FastLane.Academy.dto.LoginResponseDTO;
import com.example.FastLane.Academy.dto.RegisterDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Admin;
import com.example.FastLane.Academy.entity.Instructor;
import com.example.FastLane.Academy.entity.Student;
import com.example.FastLane.Academy.enums.InstructorStatus;
import com.example.FastLane.Academy.enums.StudentStatus;
import com.example.FastLane.Academy.enums.UserRole;
import com.example.FastLane.Academy.repo.InstructorRepo;
import com.example.FastLane.Academy.repo.StudentRepo;
import com.example.FastLane.Academy.repo.AdminRepo;
import com.example.FastLane.Academy.util.VarList;
import jakarta.servlet.http.HttpSession;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Service
@Transactional
public class AuthService {

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private InstructorRepo instructorRepo;

    @Autowired
    private AdminRepo adminRepo;

    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private ModelMapper modelMapper;


    public ResponseDTO registerStudent(RegisterDTO registerDTO) {

        if (studentRepo.existsByEmail(registerDTO.getEmail()))
            return new ResponseDTO(VarList.DUPLICATE_EMAIL, "Email exists", null);

        if (studentRepo.existsByNic(registerDTO.getNic()))
            return new ResponseDTO(VarList.DUPLICATE_NIC, "NIC exists", null);

        int age = Period.between(registerDTO.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 18) {
            return new ResponseDTO(VarList.INVALID_AGE, "Student must be at least 18 years old", null);
        }

        Student student = new Student();
        student.setStudentId(generateStudentId());
        student.setFullName(registerDTO.getFullName());
        student.setEmail(registerDTO.getEmail());
        student.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        student.setContactNumber(registerDTO.getContactNumber());
        student.setNic(registerDTO.getNic());
        student.setAddress(registerDTO.getAddress());
        student.setDateOfBirth(registerDTO.getDateOfBirth());
        student.setEmergencyContact(registerDTO.getEmergencyContact());

        student.setRole(UserRole.STUDENT);
        student.setStatus(StudentStatus.ACTIVE);
        student.setRegisteredDate(LocalDate.now());

        studentRepo.save(student);

        return new ResponseDTO(VarList.RSP_SUCCESS, "Student registered", null);
    }
    public ResponseDTO registerInstructor(RegisterDTO registerDTO) {

        if (instructorRepo.existsByEmail(registerDTO.getEmail()))
            return new ResponseDTO(VarList.DUPLICATE_EMAIL, "Email already exists", null);

        if (instructorRepo.existsByLicenseId(registerDTO.getLicenseId()))
            return new ResponseDTO(VarList.DUPLICATE_LICENSE, "License already exists", null);

        Instructor instructor = new Instructor();

        instructor.setInstructorId(generateInstructorId());
        instructor.setInstructorName(registerDTO.getFullName());
        instructor.setEmail(registerDTO.getEmail());
        instructor.setContactNumber(registerDTO.getContactNumber());
        instructor.setExperienceYears(registerDTO.getExperienceYears());
        instructor.setVehicleType(registerDTO.getVehicleType());

        instructor.setLicenseId(registerDTO.getLicenseId());

        // password hashing
        instructor.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        instructor.setWorkingDays(registerDTO.getWorkingDays());

        instructor.setRole(UserRole.INSTRUCTOR);
        instructor.setStatus(InstructorStatus.ACTIVE);

        instructorRepo.save(instructor);

        return new ResponseDTO(VarList.RSP_SUCCESS, "Instructor registered", null);
    }

    public ResponseDTO login(LoginDTO loginDTO, HttpSession session) {

        // STUDENT
        Optional<Student> student = studentRepo.findByEmail(loginDTO.getEmail());
        if (student.isPresent() &&
                passwordEncoder.matches(loginDTO.getPassword(), student.get().getPassword())) {

            session.setAttribute("userId", student.get().getStudentId());
            session.setAttribute("role", UserRole.STUDENT.name());

            return new ResponseDTO(VarList.RSP_SUCCESS,
                    "Student login success",
                    new LoginResponseDTO(student.get().getStudentId(),
                            student.get().getEmail(),
                            "STUDENT"));
        }

        // INSTRUCTOR
        Optional<Instructor> instructor = instructorRepo.findByEmail(loginDTO.getEmail());
        if (instructor.isPresent() &&
                passwordEncoder.matches(loginDTO.getPassword(), instructor.get().getPassword())) {

            session.setAttribute("userId", instructor.get().getInstructorId());
            session.setAttribute("role", UserRole.INSTRUCTOR.name());

            return new ResponseDTO(VarList.RSP_SUCCESS,
                    "Instructor login success",
                    new LoginResponseDTO(instructor.get().getInstructorId(),
                            instructor.get().getEmail(),
                            "INSTRUCTOR"));
        }

        // ADMIN
        Optional<Admin> admin = adminRepo.findByEmail(loginDTO.getEmail());
        if (admin.isPresent() &&
                passwordEncoder.matches(loginDTO.getPassword(), admin.get().getPassword())) {

            session.setAttribute("userId", admin.get().getAdminId());
            session.setAttribute("role", UserRole.ADMIN.name());

            return new ResponseDTO(VarList.RSP_SUCCESS,
                    "Admin login success",
                    new LoginResponseDTO(admin.get().getAdminId(),
                            admin.get().getEmail(),
                            "ADMIN"));
        }

        return new ResponseDTO(VarList.UNAUTHORIZED, "Invalid credentials", null);
    }

    public ResponseDTO logout(HttpSession session) {
        session.invalidate();
        return new ResponseDTO(VarList.RSP_SUCCESS, "Logged out", null);
    }


    //ID Generation

    private String generateStudentId() {

        String lastId = studentRepo
                .findTopByOrderByStudentIdDesc()
                .map(Student::getStudentId)
                .orElse(null);

        if (lastId == null) {
            return "S001";
        }

        int number = Integer.parseInt(lastId.substring(1));

        return String.format("S%03d", number + 1);
    }

    private String generateInstructorId() {

        String lastId = instructorRepo
                .findTopByOrderByInstructorIdDesc()
                .map(Instructor::getInstructorId)
                .orElse(null);

        if (lastId == null) {
            return "I001";
        }

        int number = Integer.parseInt(lastId.substring(1));

        return String.format("I%03d", number + 1);
    }
}
