package com.example.FastLane.Academy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Feedback {

    @Id
    private String  feedbackId;

    private String studentId;
    private int rating;
    private String comment;
    private LocalDate feedbackDate;


}