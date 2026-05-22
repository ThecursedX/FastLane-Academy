package com.example.FastLane.Academy.dto;

import com.example.FastLane.Academy.enums.WorkingDay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstructorSlotDTO {
    private String slotId;
    private String instructorId;
    private String courseId;
    private WorkingDay dayOfWeek;
    private LocalTime time;
    private Boolean enabled;          // wrapper — allows null when not sent in request body
    private Integer activeRequestCount; // wrapper — computed server-side, nullable in requests
    private String instructorName;    // populated server-side for display
    private Integer experienceYears;  // populated server-side for experience filter/sort
}