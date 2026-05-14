package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.dto.StudentDTO;
import com.example.FastLane.Academy.entity.Student;
import com.example.FastLane.Academy.enums.StudentStatus;
import com.example.FastLane.Academy.enums.UserRole;
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