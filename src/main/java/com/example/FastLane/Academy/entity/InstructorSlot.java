package com.example.FastLane.Academy.entity;

import com.example.FastLane.Academy.enums.WorkingDay;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "instructor_slots",
       uniqueConstraints = @UniqueConstraint(columnNames = {"instructorId","courseId","dayOfWeek","time"}))
public class InstructorSlot {

    @Id
    private String slotId;           // e.g. IS001

    private String instructorId;
    private String courseId;        // which course this slot is for

    @Enumerated(EnumType.STRING)
    private WorkingDay dayOfWeek;    // reuse existing enum

    private LocalTime time;          // must be one of the 5 allowed time slots

    private boolean enabled;         // instructor toggles; can only disable if no active requests
}