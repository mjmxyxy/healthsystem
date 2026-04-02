package com.healthy.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Appointment {
    private Long id;
    private Long studentId;
    private Long counselorId;
    private String startTime; // ISO string
    private String endTime;
    private String status; // PENDING, CONFIRMED, CANCELLED
    private LocalDateTime createdAt;
}
