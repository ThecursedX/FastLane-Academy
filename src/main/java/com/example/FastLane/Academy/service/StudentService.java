package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.dto.StudentDTO;
import com.example.FastLane.Academy.entity.Student;
import com.example.FastLane.Academy.enums.StudentStatus;
import com.example.FastLane.Academy.repo.StudentRepo;
import com.example.FastLane.Academy.util.VarList;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private ModelMapper modelMapper;

    public ResponseDTO registerStudent(StudentDTO dto) {

        // NIC duplicate validation
        if (studentRepo.existsByNic(dto.getNic())) {
            return new ResponseDTO(
                    VarList.DUPLICATE_NIC,
                    "NIC already exists",
                    null
            );
        }

        // Email duplicate validation
        if (studentRepo.existsByEmail(dto.getEmail())) {
            return new ResponseDTO(
                    VarList.DUPLICATE_EMAIL,
                    "Email already exists",
                    null
            );
        }

        // Age validation
        int age = Period.between(
                dto.getDateOfBirth(),
                LocalDate.now()
        ).getYears();

        if (age < 18) {
            return new ResponseDTO(
                    VarList.INVALID_AGE,
                    "Student must be at least 18 years old",
                    null
            );
        }

        Student student = modelMapper.map(dto, Student.class);

        // Generate ID
        String lastId = studentRepo
                .findTopByOrderByStudentIdDesc()
                .map(Student::getStudentId)
                .orElse(null);

        String nextId;

        if (lastId == null) {
            nextId = "S001";
        } else {

            int number = Integer.parseInt(lastId.substring(1));
            nextId = String.format("S%03d", number + 1);
        }

        student.setStudentId(nextId);

        student.setStatus(StudentStatus.ACTIVE);

        student.setRegisteredDate(LocalDate.now());

        studentRepo.save(student);

        return new ResponseDTO(
                VarList.RSP_SUCCESS,
                "Student registered successfully",
                student
        );
    }

    public ResponseDTO updateStudent(StudentDTO dto) {

        Optional<Student> optionalStudent =
                studentRepo.findById(dto.getStudentId());

        if (optionalStudent.isEmpty()) {
            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND,
                    "Student not found",
                    null
            );
        }

        Student student = optionalStudent.get();

        student.setFullName(dto.getFullName());
        student.setEmail(dto.getEmail());
        student.setContactNumber(dto.getContactNumber());
        student.setAddress(dto.getAddress());
        student.setEmergencyContact(dto.getEmergencyContact());

        studentRepo.save(student);

        return new ResponseDTO(
                VarList.UPDATED_SUCCESSFULLY,
                "Student updated successfully",
                student
        );
    }

    public ResponseDTO deactivateStudent(String studentId) {

        Optional<Student> optionalStudent =
                studentRepo.findById(studentId);

        if (optionalStudent.isEmpty()) {
            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND,
                    "Student not found",
                    null
            );
        }

        Student student = optionalStudent.get();

        student.setStatus(StudentStatus.INACTIVE);

        studentRepo.save(student);

        return new ResponseDTO(
                VarList.RSP_SUCCESS,
                "Student deactivated successfully",
                student
        );
    }

    public List<StudentDTO> getAllStudents() {
        List<Student> students = studentRepo.findAll();

        if (students.isEmpty()) {
            return new ArrayList<>();
        }

        return students.stream()
                .map(student -> modelMapper.map(student, StudentDTO.class))
                .toList();
    }

    public ResponseDTO getStudentById(String studentId) {
        Optional<Student> optionalStudent = studentRepo.findById(studentId);

        if (optionalStudent.isEmpty()) {
            return new ResponseDTO(
                    VarList.STUDENT_NOT_FOUND,
                    "Student not found",
                    null
            );
        }

        StudentDTO dto = modelMapper.map(optionalStudent.get(), StudentDTO.class);

        return new ResponseDTO(
                VarList.RSP_SUCCESS,
                "Student retrieved successfully",
                dto
        );
    }
}