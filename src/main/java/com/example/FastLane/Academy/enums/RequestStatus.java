package com.example.FastLane.Academy.enums;

public enum RequestStatus {
    PENDING,     // student requested slot; instructor hasn't enabled it yet
    IN_QUEUE,    // instructor enabled slot; no one confirmed yet — FIFO will pick next
    SLOT_TAKEN,  // another student is already SELECTED for this slot+date; waiting in case they cancel
    SELECTED,    // FIFO picked this student → lesson auto-created as SCHEDULED
    CANCELLED,   // student cancelled or rescheduled
    DISABLED     // instructor disabled the slot while this request was active → student can cancel/reschedule
}