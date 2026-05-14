package com.example.FastLane.Academy.repo;

import com.example.FastLane.Academy.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, String> {
    Optional<Feedback> findTopByOrderByFeedbackIdDesc();
}