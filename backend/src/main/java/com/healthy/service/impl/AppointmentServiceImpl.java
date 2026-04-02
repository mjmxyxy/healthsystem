package com.healthy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthy.entity.AppointmentRecord;
import com.healthy.mapper.AppointmentMapper;
import com.healthy.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public AppointmentRecord create(Long studentId, Long counselorId, LocalDateTime apptTime, Integer duration, String note) {
        AppointmentRecord record = new AppointmentRecord();
        record.setUserId(studentId);
        record.setCounselorId(counselorId);
        record.setApptTime(apptTime);
        record.setDuration(duration == null || duration <= 0 ? 30 : duration);
        record.setStatus(0);
        record.setNote(note == null ? "" : note);
        record.setCreateTime(LocalDateTime.now());
        appointmentMapper.insert(record);
        return record;
    }

    @Override
    public List<AppointmentRecord> listByStudent(Long studentId) {
        return appointmentMapper.selectList(new QueryWrapper<AppointmentRecord>()
                .eq("user_id", studentId)
                .orderByDesc("create_time"));
    }

    @Override
    public AppointmentRecord findById(Long id) {
        return appointmentMapper.selectById(id);
    }

    @Override
    public boolean cancel(Long id, Long studentId) {
        AppointmentRecord record = appointmentMapper.selectById(id);
        if (record == null) return false;
        if (studentId != null && !studentId.equals(record.getUserId())) return false;
        if (record.getStatus() != null && record.getStatus() == 2) return false;
        record.setStatus(3);
        return appointmentMapper.updateById(record) > 0;
    }

    @Override
    public boolean confirm(Long id, Long counselorId) {
        AppointmentRecord record = appointmentMapper.selectById(id);
        if (record == null) return false;
        if (counselorId != null && !counselorId.equals(record.getCounselorId())) return false;
        record.setStatus(1);
        return appointmentMapper.updateById(record) > 0;
    }
}
