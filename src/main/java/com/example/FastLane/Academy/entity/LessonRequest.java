package com.example.FastLane.Academy.entity;

import com.example.FastLane.Academy.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lesson_requests")
public class LessonRequest {

    @Id
    private String requestId;        // e.g. LR001

    private String slotId;           // FK → InstructorSlot (day+time template)
    private String studentId;
    private String courseId;
    private String instructorId;

    private LocalDate date;          // specific date student wants (must match slot.dayOfWeek)

    private LocalDateTime requestedAt; // FIFO key — set on creation

    @Enumerated(EnumType.STRING)
    private RequestStatus status;    // PENDING | IN_QUEUE | SELECTED | CANCELLED

    private String lessonId;         // filled once FIFO picks this request → SELECTED
}