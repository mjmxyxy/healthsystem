package com.healthy.service;

import com.healthy.entity.AppointmentRecord;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {
    AppointmentRecord create(Long studentId, Long counselorId, LocalDateTime apptTime, Integer duration, String note);
    List<AppointmentRecord> listByStudent(Long studentId);
    AppointmentRecord findById(Long id);
    boolean cancel(Long id, Long studentId);
    boolean confirm(Long id, Long counselorId);
}
