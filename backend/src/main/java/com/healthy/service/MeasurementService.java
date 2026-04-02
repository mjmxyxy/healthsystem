package com.healthy.service;

import com.healthy.entity.Question;
import com.healthy.entity.Report;

import java.util.List;
import java.util.Map;

public interface MeasurementService {
    List<Map<String, Object>> listScales();
    List<Question> listQuestionsByScale(Long scaleId, int page, int pageSize);
    Report submitAnswers(Long userId, Long scaleId, List<Map<String, Object>> answers);
    List<Report> listReportsByUser(Long userId);
    Report getReportById(Long id);
}
