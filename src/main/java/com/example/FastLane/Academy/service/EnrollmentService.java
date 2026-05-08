package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.EnrollmentDTO;
import com.example.FastLane.Academy.entity.Course;
import com.example.FastLane.Academy.repo.CourseRepo;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Enrollment;
import com.example.FastLane.Academy.entity.EnrollmentStatus;
import com.example.FastLane.Academy.repo.EnrollmentRepo;
import com.example.FastLane.Academy.util.VarList;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EnrollmentService {

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private ModelMapper modelMapper;

    // Enroll Student
    public ResponseDTO enrollStudent(
            EnrollmentDTO enrollmentDTO) {

        // Duplicate enrollment validation
        boolean alreadyExists =
                enrollmentRepo.existsByStudentIdAndCourseId(
                        enrollmentDTO.getStudentId(),
                        enrollmentDTO.getCourseId()
                );

        if (alreadyExists) {

            return new ResponseDTO(
                    VarList.ALREADY_ENROLLED, "Student already enrolled in this course", enrollmentDTO);
        }

        // Check course exists
        Optional<Course> optionalCourse =
                courseRepo.findById(
                        enrollmentDTO.getCourseId());

        if (optionalCourse.isEmpty()) {

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND, "Course not found", null);
        }

        Enrollment enrollment =
                modelMapper.map(enrollmentDTO,
                        Enrollment.class);

        enrollment.setEnrolledDate(LocalDate.now());

        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        enrollmentRepo.save(enrollment);

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Student enrolled successfully", enrollment);
    }

    // Get Courses By Student
    public ResponseDTO getCoursesByStudent(String studentId) {

        List<EnrollmentDTO> list =
                enrollmentRepo.findByStudentId(studentId)
                        .stream()
                        .map(enrollment ->
                                modelMapper.map(enrollment,
                                        EnrollmentDTO.class))
                        .toList();

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Student enrollments retrieved successfully", list);
    }

    // Get Students By Course
    public ResponseDTO getStudentsByCourse(String courseId) {

        List<EnrollmentDTO> list =
                enrollmentRepo.findByCourseId(courseId)
                        .stream()
                        .map(enrollment ->
                                modelMapper.map(enrollment,
                                        EnrollmentDTO.class))
                        .toList();

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Course enrollments retrieved successfully", list);
    }

    // Remove Enrollment
    public ResponseDTO removeEnrollment(String enrollmentId) {

        Optional<Enrollment> optionalEnrollment =
                enrollmentRepo.findById(enrollmentId);

        if (optionalEnrollment.isEmpty()) {

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND, "Enrollment not found", null);
        }

        Enrollment enrollment = optionalEnrollment.get();

        enrollment.setStatus(EnrollmentStatus.CANCELLED);

        enrollmentRepo.save(enrollment);

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Enrollment removed successfully", enrollment);
    }
}