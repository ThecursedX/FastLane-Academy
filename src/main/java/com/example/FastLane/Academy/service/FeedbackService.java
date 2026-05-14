package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.FeedbackDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Feedback;
import com.example.FastLane.Academy.entity.Student;
import com.example.FastLane.Academy.repo.FeedbackRepository;
import com.example.FastLane.Academy.repo.StudentRepo;
import com.example.FastLane.Academy.util.VarList;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private ModelMapper modelMapper;

    public ResponseDTO createFeedback(FeedbackDTO feedbackDTO,String studentId) {

        Optional<Student> optionalStudent =
                studentRepo.findById(studentId);

        if(optionalStudent.isEmpty()) {
            return new ResponseDTO(VarList.STUDENT_NOT_FOUND, "Student not found", null);
        }

        if(feedbackDTO.getRating() < 1 || feedbackDTO.getRating() > 5) {

            return new ResponseDTO(VarList.RSP_FAIL, "Rating must be between 1 and 5", null);
        }
        Feedback feedback =
                modelMapper.map(feedbackDTO, Feedback.class);

        feedback.setStudentId(studentId);
        feedback.setFeedbackId(generateFeedbackId());
        feedback.setFeedbackDate(LocalDate.now());

        feedbackRepository.save(feedback);

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Feedback added successfully", feedback);
    }

    public List<FeedbackDTO> getAllFeedback() {
        List<Feedback> feedbackList =
                feedbackRepository.findAll();

        return modelMapper.map(
                feedbackList,
                new TypeToken<ArrayList<FeedbackDTO>>(){}.getType());
    }

    public ResponseDTO updateFeedback(FeedbackDTO feedbackDTO) {
        Optional<Feedback> optionalFeedback =
                feedbackRepository.findById(feedbackDTO.getFeedbackId());

        if(optionalFeedback.isEmpty()) {

            return new ResponseDTO(VarList.RSP_NO_DATA_FOUND, "Feedback not found", null);
        }

        Feedback feedback = optionalFeedback.get();

        feedback.setRating(feedbackDTO.getRating());
        feedback.setComment(feedbackDTO.getComment());

        feedbackRepository.save(feedback);

        return new ResponseDTO(VarList.RSP_SUCCESS, "Feedback updated successfully", null);
    }

    public ResponseDTO deleteFeedback(String feedbackId) {

        if(!feedbackRepository.existsById(feedbackId)) {

            return new ResponseDTO(VarList.RSP_NO_DATA_FOUND, "Feedback not found", null);
        }

        feedbackRepository.deleteById(feedbackId);

        return new ResponseDTO(VarList.RSP_SUCCESS, "Feedback deleted successfully", null);
    }
    private String generateFeedbackId() {

        String lastId = feedbackRepository
                .findTopByOrderByFeedbackIdDesc()
                .map(Feedback::getFeedbackId)
                .orElse(null);

        if(lastId == null) {
            return "F001";
        }

        int number =
                Integer.parseInt(lastId.substring(1));

        return String.format("F%03d", number + 1);
    }
}