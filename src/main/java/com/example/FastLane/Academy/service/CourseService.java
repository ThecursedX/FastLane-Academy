package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.CourseDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Course;
import com.example.FastLane.Academy.enums.CourseStatus;
import com.example.FastLane.Academy.enums.DifficultyLevel;
import com.example.FastLane.Academy.repo.CourseRepo;
import com.example.FastLane.Academy.repo.EnrollmentRepo;
import com.example.FastLane.Academy.util.VarList;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    // TODO: integrate with Student module later
    /*@Autowired
    private StudentRepo studentRepo;*/

    // Add Course
    public ResponseDTO addCourse(CourseDTO courseDTO) {

        if (courseRepo.existsByCourseTitle(
                courseDTO.getCourseTitle())) {

            return new ResponseDTO(
                    VarList.DUPLICATE_COURSE, "Course title already exists", courseDTO);
        }

        Course course =
                modelMapper.map(courseDTO, Course.class);

        course.setStatus(CourseStatus.ACTIVE);
        course.setLastUpdated(LocalDateTime.now());

        String lastId = courseRepo.findTopByOrderByCourseIdDesc()
                .map(Course::getCourseId)
                .orElse(null);

        String nextId;

        if (lastId == null) {
            nextId = "C001";
        } else {

            int number = Integer.parseInt(lastId.substring(1));
            nextId = String.format("C%03d", number + 1);
        }

        course.setCourseId(nextId);

        courseRepo.save(course);

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Course added successfully", courseDTO);
    }

    // Get All Courses
    public List<CourseDTO> getAllCourses() {

        return courseRepo.findAll().stream()
                .map(course ->
                        modelMapper.map(course, CourseDTO.class))
                .toList();
    }

    // Get Course By ID
    public ResponseDTO getCourseById(String courseId) {

        Optional<Course> optionalCourse =
                courseRepo.findById(courseId);

        if (optionalCourse.isEmpty()) {

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND, "Course not found", null);
        }

        CourseDTO courseDTO =
                modelMapper.map(optionalCourse.get(),
                        CourseDTO.class);

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Course retrieved successfully", courseDTO);
    }

    // Update Course
    public ResponseDTO updateCourse(CourseDTO courseDTO) {

        Optional<Course> optionalCourse =
                courseRepo.findById(courseDTO.getCourseId());

        if (optionalCourse.isEmpty()) {

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND, "Course not found", courseDTO);
        }

        Course existingCourse = optionalCourse.get();

        // PATCH style updates

        if (courseDTO.getCourseTitle() != null) {
            existingCourse.setCourseTitle(
                    courseDTO.getCourseTitle());
        }

        if (courseDTO.getDescription() != null) {
            existingCourse.setDescription(
                    courseDTO.getDescription());
        }

        if (courseDTO.getDurationHours() > 0) {
            existingCourse.setDurationHours(
                    courseDTO.getDurationHours());
        }

        if (courseDTO.getDifficultyLevel() != null) {
            existingCourse.setDifficultyLevel(
                    courseDTO.getDifficultyLevel());
        }

        if (courseDTO.getSyllabus() != null) {
            existingCourse.setSyllabus(
                    courseDTO.getSyllabus());
        }

        if (courseDTO.getContentStructure() != null) {
            existingCourse.setContentStructure(
                    courseDTO.getContentStructure());
        }

        existingCourse.setLastUpdated(LocalDateTime.now());

        courseRepo.save(existingCourse);

        return new ResponseDTO(
                VarList.UPDATED_SUCCESSFULLY, "Course updated successfully", courseDTO);
    }

    // Soft Delete Course
    public ResponseDTO deleteCourse(String courseId) {

        Optional<Course> optionalCourse =
                courseRepo.findById(courseId);

        if (optionalCourse.isEmpty()) {

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND, "Course not found", null);
        }

        Course course = optionalCourse.get();

        boolean hasEnrollments = !enrollmentRepo.findByCourseId(courseId).isEmpty();


        if (hasEnrollments) {

            return new ResponseDTO(
                    VarList.COURSE_HAS_ENROLLMENTS, "Cannot delete enrolled course", course);
        }

        course.setStatus(CourseStatus.INACTIVE);

        courseRepo.save(course);

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Course deleted successfully", course);
    }


    // Permanently Delete Archived Course
    public ResponseDTO deleteArchivedCourse(String courseId) {

        Optional<Course> optionalCourse = courseRepo.findById(courseId);

        if (optionalCourse.isEmpty()) {
            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND, "Course not found", null);
        }

        Course course = optionalCourse.get();

        if (course.getStatus() != CourseStatus.INACTIVE) {
            return new ResponseDTO(
                    VarList.COURSE_NOT_ARCHIVED, "Only archived courses can be deleted permanently", course);
        }

        boolean hasEnrollments = !enrollmentRepo.findByCourseId(courseId).isEmpty();

        if (hasEnrollments) {
            return new ResponseDTO(
                    VarList.COURSE_HAS_ENROLLMENTS, "Cannot permanently delete course with enrollments", course);
        }

        courseRepo.delete(course);

        return new ResponseDTO(
                VarList.COURSE_DELETED, "Archived course deleted permanently", course);
    }

    // Filter By Difficulty Level
    public ResponseDTO getCoursesByDifficulty(
            DifficultyLevel difficultyLevel) {

        List<CourseDTO> list =
                courseRepo.findByDifficultyLevelAndStatus(
                                difficultyLevel,
                                CourseStatus.ACTIVE
                        )
                        .stream()
                        .map(course ->
                                modelMapper.map(course,
                                        CourseDTO.class))
                        .toList();

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Courses retrieved successfully", list);
    }


}