package com.healthy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.healthy.entity.Question;
import com.healthy.entity.Report;
import com.healthy.entity.Scale;
import com.healthy.mapper.QuestionMapper;
import com.healthy.mapper.ReportMapper;
import com.healthy.mapper.ScaleMapper;
import com.healthy.service.MeasurementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MeasurementServiceImpl implements MeasurementService {

    @Autowired
    private ScaleMapper scaleMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ReportMapper reportMapper;

    private final ObjectMapper json = new ObjectMapper();

    @Override
    public List<Map<String, Object>> listScales() {
        List<Scale> list = scaleMapper.selectList(new QueryWrapper<>());
        List<Map<String, Object>> out = new ArrayList<>();
        for (Scale s : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("code", s.getCode());
            m.put("name", s.getName());
            m.put("description", s.getDescription());
            m.put("questionCount", s.getQuestionCount());
            out.add(m);
        }
        return out;
    }

    @Override
    public List<Question> listQuestionsByScale(Long scaleId, int page, int pageSize) {
        Page<Question> p = new Page<>(page, pageSize);
        QueryWrapper<Question> q = new QueryWrapper<>();
        q.eq("scale_id", scaleId).orderByAsc("question_no");
        Page<Question> res = questionMapper.selectPage(p, q);
        for (Question question : res.getRecords()) {
            if (question.getMaxScore() == null) question.setMaxScore(4);
        }
        return res.getRecords();
    }

    @Override
    public Report submitAnswers(Long userId, Long scaleId, List<Map<String, Object>> answers) {
        // validate questions count
        List<Question> all = questionMapper.selectList(new QueryWrapper<Question>().eq("scale_id", scaleId).orderByAsc("question_no"));
        if (answers == null) throw new IllegalArgumentException("answers required");
        if (all.isEmpty()) throw new IllegalArgumentException("当前量表未配置题目");
        if (answers.size() != all.size()) {
            // allow partial? for now require full
            throw new IllegalArgumentException("incomplete answers");
        }
        double total = 0;
        Map<Long, Integer> map = new LinkedHashMap<>();
        Map<Long, Question> questionMap = all.stream().collect(Collectors.toMap(Question::getId, q -> q));
        Map<String, Double> dimensionScores = new LinkedHashMap<>();
        for (Map<String, Object> a : answers) {
            Number qidN = (Number)a.get("questionId");
            Number ansN = (Number)a.get("answer");
            if (qidN == null || ansN == null) throw new IllegalArgumentException("invalid answer item");
            long qid = qidN.longValue();
            int ans = ansN.intValue();
            map.put(qid, ans);
            total += ans;

            Question q = questionMap.get(qid);
            if (q != null && q.getDimension() != null && !q.getDimension().isBlank()) {
                dimensionScores.put(q.getDimension(), dimensionScores.getOrDefault(q.getDimension(), 0.0) + ans);
            }
        }
        double avg = total / all.size();

        String level;
        int levelCode;
        if (avg <= 2.0) level = "正常";
        else if (avg <= 3.0) level = "轻度";
        else if (avg <= 4.0) level = "中度";
        else level = "重度";
        if (avg <= 2.0) levelCode = 0;
        else if (avg <= 3.0) levelCode = 1;
        else if (avg <= 4.0) levelCode = 2;
        else levelCode = 3;

        Report r = new Report();
        r.setUserId(userId);
        r.setScaleId(scaleId);
        r.setTotalScore(total);
        r.setAvgScore(avg);
        try {
            r.setDimensionScores(json.writeValueAsString(dimensionScores));
        } catch (Exception ex) {
            r.setDimensionScores("{}");
        }
        r.setLevel(level);
        r.setLevelCode(levelCode);
        r.setTestTime(java.time.LocalDateTime.now());
        try {
            String answersJson = json.writeValueAsString(map);
            r.setAnswers(answersJson);
            r.setDetails("{" + "\"summary\":\"测评完成\",\"level\":\"" + level + "\"}");
        } catch (Exception ex) {
            r.setAnswers("{}");
            r.setDetails("{}");
        }
        reportMapper.insert(r);
        return r;
    }

    @Override
    public List<Report> listReportsByUser(Long userId) {
        QueryWrapper<Report> q = new QueryWrapper<>();
        q.eq("user_id", userId).orderByDesc("create_time");
        return reportMapper.selectList(q);
    }

    @Override
    public Report getReportById(Long id) {
        return reportMapper.selectById(id);
    }
}
