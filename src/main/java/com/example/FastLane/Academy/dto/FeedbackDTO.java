package com.example.FastLane.Academy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDTO {

    private String feedbackId;
    private int rating;
    private String comment;
    private LocalDate feedbackDate;
}