package com.healthy.controller;

import com.healthy.entity.Question;
import com.healthy.entity.Report;
import com.healthy.entity.Scale;
import com.healthy.mapper.ScaleMapper;
import com.healthy.service.MeasurementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scale")
@CrossOrigin(origins = "http://localhost:5173")
public class MeasurementController {

    @Autowired
    private MeasurementService measurementService;

    @Autowired
    private ScaleMapper scaleMapper;

    @GetMapping("")
    public ResponseEntity<?> listScales() {
        return ResponseEntity.ok(measurementService.listScales());
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<?> listQuestions(@PathVariable Long id,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        List<Question> qs = measurementService.listQuestionsByScale(id, page, pageSize);
        return ResponseEntity.ok(qs);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submit(@PathVariable Long id, @RequestBody Map<String, Object> body, Principal principal) {
        Long userId = 0L;
        try {
            Object bodyUserId = body.get("userId");
            if (bodyUserId != null) userId = Long.parseLong(String.valueOf(bodyUserId));
            if (userId <= 0 && principal != null) userId = Long.parseLong(principal.getName());
        } catch (Exception ignore) {}
        if (userId <= 0) {
            throw new IllegalArgumentException("请先登录后再提交测评");
        }
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.get("answers");
        Report r = measurementService.submitAnswers(userId, id, answers);
        return ResponseEntity.ok(r);
    }

    @GetMapping("/report")
    public ResponseEntity<?> listReports(@RequestParam(required = false) Long userId, Principal principal) {
        Long uid = 0L;
        try { if (userId != null) uid = userId; else if (principal != null) uid = Long.parseLong(principal.getName()); } catch (Exception ignored) {}
        List<Report> list = measurementService.listReportsByUser(uid);
        List<Map<String, Object>> out = list.stream().map(r -> {
            Scale s = scaleMapper.selectById(r.getScaleId());
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", r.getId());
            item.put("scaleId", r.getScaleId());
            item.put("scaleName", s == null ? "未知量表" : s.getName());
            item.put("totalScore", r.getTotalScore());
            item.put("avgScore", r.getAvgScore());
            item.put("level", r.getLevel());
            item.put("createdAt", r.getCreatedAt() == null ? r.getTestTime() : r.getCreatedAt());
            return item;
        }).toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping("/report/{id}")
    public ResponseEntity<?> getReport(@PathVariable Long id) {
        Report r = measurementService.getReportById(id);
        if (r == null) return ResponseEntity.notFound().build();
        Scale s = scaleMapper.selectById(r.getScaleId());
        String suggestion;
        String level = r.getLevel() == null ? "正常" : r.getLevel();
        if ("正常".equals(level)) suggestion = "当前状态总体稳定，建议保持规律作息与适量运动。";
        else if ("轻度".equals(level)) suggestion = "近期可能有轻度压力，建议进行放松训练并与信任的人交流。";
        else if ("中度".equals(level)) suggestion = "建议尽快预约咨询师进行进一步评估和干预。";
        else suggestion = "建议尽快联系学校心理中心或专业机构，获得及时支持。";
        return ResponseEntity.ok(Map.of(
                "id", r.getId(),
                "scaleId", r.getScaleId(),
                "scaleName", s == null ? "未知量表" : s.getName(),
                "totalScore", r.getTotalScore(),
                "avgScore", r.getAvgScore(),
                "level", level,
                "suggestion", suggestion,
            "dimensionScores", r.getDimensionScores() == null ? "{}" : r.getDimensionScores(),
                "answers", r.getAnswers() == null ? "{}" : r.getAnswers(),
                "createdAt", r.getCreatedAt() == null ? r.getTestTime() : r.getCreatedAt()
        ));
    }
}
