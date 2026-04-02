package com.healthy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthy.entity.AppointmentRecord;
import com.healthy.entity.User;
import com.healthy.mapper.AppointmentMapper;
import com.healthy.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/counselors")
@CrossOrigin(origins = "http://localhost:5173")
public class CounselorController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @GetMapping("")
    public ResponseEntity<?> list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String gender) {
        List<User> list = userMapper.selectList(new QueryWrapper<User>().eq("role", "counselor"));
        List<Map<String, Object>> out = new ArrayList<>();
        for (User u : list) {
            String name = u.getName() == null ? "未命名咨询师" : u.getName();
            String account = u.getAccount() == null ? "" : u.getAccount();
            String g = u.getGender() == null ? "" : u.getGender();
            boolean hitKeyword = keyword == null || keyword.isBlank() || name.contains(keyword) || account.contains(keyword);
            boolean hitGender = gender == null || gender.isBlank() || gender.equals(g);
            if (!hitKeyword || !hitGender) continue;

            out.add(Map.of(
                    "id", u.getId(),
                    "name", name,
                    "account", account,
                    "gender", g,
                    "title", "国家二级心理咨询方向",
                    "specialty", "情绪疏导、压力管理、学业心理",
                    "bio", "擅长青少年心理支持与短程咨询。",
                    "rating", 4.8,
                    "price", 120
            ));
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        User u = userMapper.selectById(id);
        if (u == null || u.getRole() == null || !"counselor".equals(u.getRole())) {
            return ResponseEntity.badRequest().body(Map.of("error", "counselor not found"));
        }

        LocalDate today = LocalDate.now();
        List<String> slots = new ArrayList<>();
        for (int d = 0; d < 5; d++) {
            LocalDate day = today.plusDays(d);
            slots.add(LocalDateTime.of(day, LocalTime.of(9, 0)).toString());
            slots.add(LocalDateTime.of(day, LocalTime.of(10, 0)).toString());
            slots.add(LocalDateTime.of(day, LocalTime.of(14, 0)).toString());
            slots.add(LocalDateTime.of(day, LocalTime.of(15, 0)).toString());
        }

        List<AppointmentRecord> occupied = appointmentMapper.selectList(new QueryWrapper<AppointmentRecord>()
                .eq("counselor_id", id)
                .in("status", 0, 1));
        List<String> occupiedSet = occupied.stream().map(a -> a.getApptTime().toString()).toList();
        List<String> available = slots.stream().filter(t -> !occupiedSet.contains(t)).toList();

        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "name", u.getName() == null ? "未命名咨询师" : u.getName(),
                "account", u.getAccount() == null ? "" : u.getAccount(),
                "gender", u.getGender() == null ? "" : u.getGender(),
                "title", "国家二级心理咨询方向",
                "specialty", "情绪疏导、压力管理、学业心理",
                "bio", "从业经验丰富，遵循保密原则，支持线上一对一咨询。",
                "rating", 4.8,
                "price", 120,
                "availableSlots", available
        ));
    }
}
