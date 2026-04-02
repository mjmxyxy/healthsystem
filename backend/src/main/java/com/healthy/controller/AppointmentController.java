package com.healthy.controller;

import com.healthy.entity.AppointmentRecord;
import com.healthy.entity.User;
import com.healthy.mapper.UserMapper;
import com.healthy.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:5173")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserMapper userMapper;

    private String statusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待确认";
            case 1 -> "已确认";
            case 2 -> "已完成";
            case 3 -> "已取消";
            default -> "未知";
        };
    }

    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Long studentId = Long.parseLong(String.valueOf(body.getOrDefault("studentId", "0")));
            Long counselorId = Long.parseLong(String.valueOf(body.getOrDefault("counselorId", "0")));
            String start = String.valueOf(body.getOrDefault("startTime", ""));
            Integer duration = Integer.parseInt(String.valueOf(body.getOrDefault("duration", "30")));
            String note = String.valueOf(body.getOrDefault("note", ""));
            if (studentId == 0 || counselorId == 0 || start.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "missing fields"));
            }
            LocalDateTime apptTime = LocalDateTime.parse(start);
            AppointmentRecord created = appointmentService.create(studentId, counselorId, apptTime, duration, note);
            return ResponseEntity.ok(created);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "startTime format should be yyyy-MM-ddTHH:mm:ss"));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", "internal", "detail", ex.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> list(@RequestParam(required = false) Long studentId) {
        if (studentId == null) return ResponseEntity.badRequest().body(Map.of("error", "studentId required"));
        List<AppointmentRecord> list = appointmentService.listByStudent(studentId);
        List<Map<String, Object>> out = list.stream().map(a -> {
            User counselor = userMapper.selectById(a.getCounselorId());
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("studentId", a.getUserId());
            m.put("counselorId", a.getCounselorId());
            m.put("counselorName", counselor == null ? "咨询师" : counselor.getName());
            m.put("startTime", a.getApptTime());
            m.put("duration", a.getDuration());
            m.put("note", a.getNote());
            m.put("status", statusText(a.getStatus()));
            m.put("statusCode", a.getStatus());
            return m;
        }).toList();
        return ResponseEntity.ok(out);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id, @RequestParam Long studentId) {
        boolean ok = appointmentService.cancel(id, studentId);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("error", "cancel failed"));
        return ResponseEntity.ok(Map.of("message", "cancelled"));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id, @RequestParam Long counselorId) {
        boolean ok = appointmentService.confirm(id, counselorId);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("error", "confirm failed"));
        return ResponseEntity.ok(Map.of("message", "confirmed"));
    }
}
