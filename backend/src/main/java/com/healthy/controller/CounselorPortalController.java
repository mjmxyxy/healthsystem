package com.healthy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthy.entity.AppointmentRecord;
import com.healthy.entity.CounselorReportNote;
import com.healthy.entity.CounselorStudentNote;
import com.healthy.entity.Report;
import com.healthy.entity.User;
import com.healthy.mapper.AppointmentMapper;
import com.healthy.mapper.CounselorReportNoteMapper;
import com.healthy.mapper.CounselorStudentNoteMapper;
import com.healthy.mapper.ReportMapper;
import com.healthy.mapper.UserMapper;
import com.healthy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/counselor")
@CrossOrigin(origins = "http://localhost:5173")
public class CounselorPortalController {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private CounselorStudentNoteMapper counselorStudentNoteMapper;

    @Autowired
    private CounselorReportNoteMapper counselorReportNoteMapper;

    @Autowired
    private UserService userService;

    private Long currentCounselorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalArgumentException("未登录");
        }
        boolean counselor = false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if ("ROLE_COUNSELOR".equals(ga.getAuthority())) {
                counselor = true;
                break;
            }
        }
        if (!counselor) {
            throw new IllegalArgumentException("无咨询师权限");
        }
        try {
            return Long.parseLong(String.valueOf(auth.getPrincipal()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("无效登录态");
        }
    }

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

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        Long counselorId = currentCounselorId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        long pending = appointmentMapper.selectCount(new QueryWrapper<AppointmentRecord>()
                .eq("counselor_id", counselorId)
                .eq("status", 0));

        long todayConfirmed = appointmentMapper.selectCount(new QueryWrapper<AppointmentRecord>()
                .eq("counselor_id", counselorId)
                .eq("status", 1)
                .ge("appt_time", start)
                .lt("appt_time", end));

        long upcoming = appointmentMapper.selectCount(new QueryWrapper<AppointmentRecord>()
                .eq("counselor_id", counselorId)
                .eq("status", 1)
                .ge("appt_time", now));

        List<AppointmentRecord> recentRows = appointmentMapper.selectList(new QueryWrapper<AppointmentRecord>()
                .eq("counselor_id", counselorId)
                .orderByDesc("create_time")
                .last("limit 5"));

        List<Map<String, Object>> recent = new ArrayList<>();
        for (AppointmentRecord a : recentRows) {
            User u = userMapper.selectById(a.getUserId());
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("studentId", a.getUserId());
            m.put("studentName", u == null ? "学生" : u.getName());
            m.put("startTime", a.getApptTime());
            m.put("status", statusText(a.getStatus()));
            m.put("statusCode", a.getStatus());
            recent.add(m);
        }

        return ResponseEntity.ok(Map.of(
                "pendingAppointments", pending,
                "todayConfirmed", todayConfirmed,
                "upcomingConsultations", upcoming,
                "recentAppointments", recent
        ));
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> appointments() {
        Long counselorId = currentCounselorId();
        List<AppointmentRecord> rows = appointmentMapper.selectList(new QueryWrapper<AppointmentRecord>()
                .eq("counselor_id", counselorId)
                .orderByDesc("create_time"));

        List<Map<String, Object>> out = new ArrayList<>();
        for (AppointmentRecord a : rows) {
            User u = userMapper.selectById(a.getUserId());
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("studentId", a.getUserId());
            m.put("studentName", u == null ? "学生" : u.getName());
            m.put("studentAccount", u == null ? "" : u.getAccount());
            m.put("startTime", a.getApptTime());
            m.put("duration", a.getDuration());
            m.put("note", a.getNote());
            m.put("status", statusText(a.getStatus()));
            m.put("statusCode", a.getStatus());
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }

    @PostMapping("/appointments/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        Long counselorId = currentCounselorId();
        AppointmentRecord a = appointmentMapper.selectById(id);
        if (a == null || !counselorId.equals(a.getCounselorId())) {
            return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        }
        if (a.getStatus() != null && a.getStatus() != 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "only pending can be approved"));
        }
        a.setStatus(1);
        appointmentMapper.updateById(a);
        return ResponseEntity.ok(Map.of("message", "approved"));
    }

    @PostMapping("/appointments/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        Long counselorId = currentCounselorId();
        AppointmentRecord a = appointmentMapper.selectById(id);
        if (a == null || !counselorId.equals(a.getCounselorId())) {
            return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        }
        if (a.getStatus() != null && a.getStatus() != 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "only pending can be rejected"));
        }
        String reason = body == null ? "" : String.valueOf(body.getOrDefault("reason", ""));
        a.setStatus(3);
        if (reason != null && !reason.isBlank()) {
            a.setNote((a.getNote() == null ? "" : a.getNote() + " | ") + "咨询师拒绝: " + reason);
        }
        appointmentMapper.updateById(a);
        return ResponseEntity.ok(Map.of("message", "rejected"));
    }

    @PostMapping("/appointments/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        Long counselorId = currentCounselorId();
        AppointmentRecord a = appointmentMapper.selectById(id);
        if (a == null || !counselorId.equals(a.getCounselorId())) {
            return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        }
        if (a.getStatus() != null && (a.getStatus() == 2 || a.getStatus() == 3)) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid status"));
        }
        a.setStatus(3);
        appointmentMapper.updateById(a);
        return ResponseEntity.ok(Map.of("message", "cancelled"));
    }

    @GetMapping("/students")
    public ResponseEntity<?> students() {
        Long counselorId = currentCounselorId();
        List<AppointmentRecord> rows = appointmentMapper.selectList(new QueryWrapper<AppointmentRecord>()
                .eq("counselor_id", counselorId)
                .orderByDesc("create_time"));

        Set<Long> studentIds = new HashSet<>();
        for (AppointmentRecord a : rows) {
            if (a.getUserId() != null) studentIds.add(a.getUserId());
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (Long sid : studentIds) {
            User s = userMapper.selectById(sid);
            CounselorStudentNote note = counselorStudentNoteMapper.selectOne(new QueryWrapper<CounselorStudentNote>()
                    .eq("counselor_id", counselorId)
                    .eq("student_id", sid)
                    .last("limit 1"));

            long reportCount = reportMapper.selectCount(new QueryWrapper<Report>().eq("user_id", sid));
            Map<String, Object> m = new HashMap<>();
            m.put("studentId", sid);
            m.put("name", s == null ? "学生" : s.getName());
            m.put("account", s == null ? "" : s.getAccount());
            m.put("gender", s == null ? "" : s.getGender());
            m.put("studentNo", s == null ? "" : s.getStudentId());
            m.put("reportCount", reportCount);
            m.put("note", note == null ? "" : note.getNote());
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }

    @PostMapping("/students/{studentId}/note")
    public ResponseEntity<?> saveStudentNote(@PathVariable Long studentId, @RequestBody Map<String, Object> body) {
        Long counselorId = currentCounselorId();
        String note = String.valueOf(body.getOrDefault("note", ""));

        long related = appointmentMapper.selectCount(new QueryWrapper<AppointmentRecord>()
                .eq("counselor_id", counselorId)
                .eq("user_id", studentId));
        if (related == 0) {
            return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        }

        CounselorStudentNote row = counselorStudentNoteMapper.selectOne(new QueryWrapper<CounselorStudentNote>()
                .eq("counselor_id", counselorId)
                .eq("student_id", studentId)
                .last("limit 1"));
        if (row == null) {
            row = new CounselorStudentNote();
            row.setCounselorId(counselorId);
            row.setStudentId(studentId);
            row.setNote(note);
            row.setUpdatedAt(LocalDateTime.now());
            counselorStudentNoteMapper.insert(row);
        } else {
            row.setNote(note);
            row.setUpdatedAt(LocalDateTime.now());
            counselorStudentNoteMapper.updateById(row);
        }
        return ResponseEntity.ok(Map.of("message", "saved"));
    }

    @GetMapping("/reports")
    public ResponseEntity<?> reports(@RequestParam(required = false) Long studentId) {
        Long counselorId = currentCounselorId();

        List<AppointmentRecord> related = appointmentMapper.selectList(new QueryWrapper<AppointmentRecord>()
                .eq("counselor_id", counselorId));
        Set<Long> allowedStudentIds = new HashSet<>();
        for (AppointmentRecord a : related) {
            if (a.getUserId() != null) allowedStudentIds.add(a.getUserId());
        }

        if (studentId != null && !allowedStudentIds.contains(studentId)) {
            return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        }

        QueryWrapper<Report> q = new QueryWrapper<>();
        if (studentId != null) {
            q.eq("user_id", studentId);
        } else {
            if (allowedStudentIds.isEmpty()) return ResponseEntity.ok(List.of());
            q.in("user_id", allowedStudentIds);
        }
        q.orderByDesc("create_time");

        List<Report> reports = reportMapper.selectList(q);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Report r : reports) {
            User s = userMapper.selectById(r.getUserId());
            CounselorReportNote note = counselorReportNoteMapper.selectOne(new QueryWrapper<CounselorReportNote>()
                    .eq("counselor_id", counselorId)
                    .eq("report_id", r.getId())
                    .last("limit 1"));
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("studentId", r.getUserId());
            m.put("studentName", s == null ? "学生" : s.getName());
            m.put("totalScore", r.getTotalScore());
            m.put("avgScore", r.getAvgScore());
            m.put("level", r.getLevel());
            m.put("testTime", r.getTestTime());
            m.put("dimensionScores", r.getDimensionScores());
            m.put("report", r.getDetails());
            m.put("analysis", note == null ? "" : note.getAnalysis());
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }

    @PostMapping("/reports/{reportId}/analysis")
    public ResponseEntity<?> saveReportAnalysis(@PathVariable Long reportId, @RequestBody Map<String, Object> body) {
        Long counselorId = currentCounselorId();
        Report report = reportMapper.selectById(reportId);
        if (report == null) return ResponseEntity.badRequest().body(Map.of("error", "report not found"));

        long related = appointmentMapper.selectCount(new QueryWrapper<AppointmentRecord>()
                .eq("counselor_id", counselorId)
                .eq("user_id", report.getUserId()));
        if (related == 0) {
            return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        }

        String analysis = String.valueOf(body.getOrDefault("analysis", ""));
        CounselorReportNote row = counselorReportNoteMapper.selectOne(new QueryWrapper<CounselorReportNote>()
                .eq("counselor_id", counselorId)
                .eq("report_id", reportId)
                .last("limit 1"));
        if (row == null) {
            row = new CounselorReportNote();
            row.setCounselorId(counselorId);
            row.setReportId(reportId);
            row.setAnalysis(analysis);
            row.setUpdatedAt(LocalDateTime.now());
            counselorReportNoteMapper.insert(row);
        } else {
            row.setAnalysis(analysis);
            row.setUpdatedAt(LocalDateTime.now());
            counselorReportNoteMapper.updateById(row);
        }
        return ResponseEntity.ok(Map.of("message", "saved"));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile() {
        Long counselorId = currentCounselorId();
        User u = userMapper.selectById(counselorId);
        if (u == null) return ResponseEntity.badRequest().body(Map.of("error", "user not found"));
        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "account", u.getAccount(),
                "name", u.getName(),
                "gender", u.getGender(),
                "role", u.getRole()
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> body) {
        Long counselorId = currentCounselorId();
        String name = String.valueOf(body.getOrDefault("name", ""));
        String gender = String.valueOf(body.getOrDefault("gender", ""));
        User updated = userService.updateProfile(counselorId, name, gender, null);
        if (updated == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "update failed"));
        }
        return ResponseEntity.ok(Map.of(
                "id", updated.getId(),
                "account", updated.getAccount(),
                "name", updated.getName(),
                "gender", updated.getGender(),
                "role", updated.getRole()
        ));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, Object> body) {
        Long counselorId = currentCounselorId();
        String oldPassword = String.valueOf(body.getOrDefault("oldPassword", ""));
        String newPassword = String.valueOf(body.getOrDefault("newPassword", ""));
        boolean ok = userService.changePassword(counselorId, oldPassword, newPassword);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("error", "password change failed"));
        return ResponseEntity.ok(Map.of("message", "password updated"));
    }
}
