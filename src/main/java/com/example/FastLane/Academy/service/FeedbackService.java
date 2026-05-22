package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.FeedbackDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Feedback;
import com.example.FastLane.Academy.entity.Lesson;
import com.example.FastLane.Academy.enums.LessonStatus;
import com.example.FastLane.Academy.repo.EnrollmentRepo;
import com.example.FastLane.Academy.repo.FeedbackRepository;
import com.example.FastLane.Academy.repo.LessonRepo;
import com.example.FastLane.Academy.repo.StudentRepo;
import com.example.FastLane.Academy.util.VarList;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FeedbackService {

    @Autowired private FeedbackRepository feedbackRepository;
    @Autowired private LessonRepo lessonRepo;
    @Autowired private EnrollmentRepo enrollmentRepo;
    @Autowired private StudentRepo studentRepo;
    @Autowired private ModelMapper modelMapper;

    // Student submits feedback after a lesson is COMPLETED
    public ResponseDTO createFeedback(FeedbackDTO feedbackDTO, String studentId) {

        // student must exist
        if (!studentRepo.existsById(studentId))
            return new ResponseDTO(VarList.STUDENT_NOT_FOUND, "Student not found", null);

        // student must have completed a lesson in this course
        boolean hasCompletedLesson = lessonRepo.findByStudentId(studentId).stream()
                .anyMatch(l -> feedbackDTO.getCourseId() != null
                        && feedbackDTO.getCourseId().equals(l.getCourseId())
                        && l.getStatus() == LessonStatus.COMPLETED);

        if (!hasCompletedLesson)
            return new ResponseDTO(VarList.RSP_FAIL,
                    "Feedback can only be submitted after a completed lesson in this course.", null);

        // if a specific lessonId is provided, prevent duplicate feedback for that lesson
        if (feedbackDTO.getLessonId() != null && !feedbackDTO.getLessonId().isBlank()) {
            if (feedbackRepository.existsByStudentIdAndLessonId(studentId, feedbackDTO.getLessonId()))
                return new ResponseDTO(VarList.RSP_DUPLICATED,
                        "You have already submitted feedback for this lesson.", null);
        }

        if (feedbackDTO.getRating() < 1 || feedbackDTO.getRating() > 5)
            return new ResponseDTO(VarList.RSP_FAIL, "Rating must be between 1 and 5", null);

        Feedback feedback = new Feedback();
        feedback.setFeedbackId(nextFeedbackId());
        feedback.setStudentId(studentId);
        feedback.setCourseId(feedbackDTO.getCourseId());
        feedback.setLessonId(feedbackDTO.getLessonId());
        feedback.setInstructorId(feedbackDTO.getInstructorId());
        feedback.setRating(feedbackDTO.getRating());
        feedback.setComment(feedbackDTO.getComment());
        feedback.setFeedbackDate(LocalDate.now());
        feedback.setCreatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);

        return new ResponseDTO(VarList.RSP_SUCCESS, "Feedback submitted", feedback);
    }

    public List<FeedbackDTO> getAllFeedback() {
        return modelMapper.map(feedbackRepository.findAll(),
                new TypeToken<ArrayList<FeedbackDTO>>() {}.getType());
    }

    public List<FeedbackDTO> getFeedbackByCourse(String courseId) {
        return feedbackRepository.findByCourseId(courseId).stream()
                .map(f -> modelMapper.map(f, FeedbackDTO.class)).toList();
    }

    public List<FeedbackDTO> getFeedbackByStudent(String studentId) {
        return feedbackRepository.findByStudentId(studentId).stream()
                .map(f -> modelMapper.map(f, FeedbackDTO.class)).toList();
    }

    public List<FeedbackDTO> getFeedbackByInstructor(String instructorId) {
        return feedbackRepository.findByInstructorId(instructorId).stream()
                .map(f -> modelMapper.map(f, FeedbackDTO.class)).toList();
    }

    public static final int EDIT_WINDOW_HOURS = 48;

    public ResponseDTO updateFeedback(FeedbackDTO feedbackDTO) {
        Optional<Feedback> opt = feedbackRepository.findById(feedbackDTO.getFeedbackId());
        if (opt.isEmpty())
            return new ResponseDTO(VarList.RSP_NO_DATA_FOUND, "Feedback not found", null);

        Feedback feedback = opt.get();

        // Enforce 48-hour edit window
        if (feedback.getCreatedAt() != null) {
            LocalDateTime cutoff = feedback.getCreatedAt().plusHours(EDIT_WINDOW_HOURS);
            if (LocalDateTime.now().isAfter(cutoff))
                return new ResponseDTO(VarList.RSP_FAIL,
                        "Reviews can only be edited within 48 hours of submission.", null);
        }

        if (feedbackDTO.getRating() < 1 || feedbackDTO.getRating() > 5)
            return new ResponseDTO(VarList.RSP_FAIL, "Rating must be between 1 and 5", null);

        feedback.setRating(feedbackDTO.getRating());
        feedback.setComment(feedbackDTO.getComment());
        feedbackRepository.save(feedback);
        return new ResponseDTO(VarList.RSP_SUCCESS, "Feedback updated", null);
    }

    public ResponseDTO deleteFeedback(String feedbackId) {
        if (!feedbackRepository.existsById(feedbackId))
            return new ResponseDTO(VarList.RSP_NO_DATA_FOUND, "Feedback not found", null);
        feedbackRepository.deleteById(feedbackId);
        return new ResponseDTO(VarList.RSP_SUCCESS, "Feedback deleted", null);
    }

    private String nextFeedbackId() {
        String last = feedbackRepository.findTopByOrderByFeedbackIdDesc()
                .map(Feedback::getFeedbackId).orElse(null);
        if (last == null) return "F001";
        return String.format("F%03d", Integer.parseInt(last.substring(1)) + 1);
    }
}