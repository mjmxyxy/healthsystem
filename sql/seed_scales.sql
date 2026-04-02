-- Seed file to add example scales for local demo
-- Run with: mysql -u root -p healthy_db < sql/seed_scales.sql

USE healthy_db;

INSERT INTO `scale` (name, code, description, question_count, dimensions, scoring_rules)
VALUES
('Symptom Checklist-90 (sample)', 'SCL-90', 'SCL-90 sample scale for demo purposes', 90, NULL, NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO `scale` (name, code, description, question_count, dimensions, scoring_rules)
VALUES
('Patient Health Questionnaire-9', 'PHQ-9', 'PHQ-9 简短抑郁筛查量表 (9 items)', 9, NULL, NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO `scale` (name, code, description, question_count, dimensions, scoring_rules)
VALUES
('Generalized Anxiety Disorder-7', 'GAD-7', 'GAD-7 广泛性焦虑量表 (7 items)', 7, NULL, NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO `scale` (name, code, description, question_count, dimensions, scoring_rules)
VALUES
('Depression Inventory (BDI-II)', 'BDI-II', 'Beck 抑郁量表 第二版 (21 items)', 21, NULL, NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO `scale` (name, code, description, question_count, dimensions, scoring_rules)
VALUES
('Depression Anxiety Stress Scales (DASS-21)', 'DASS-21', 'DASS-21 压力/抑郁/焦虑量表 (21 items)', 21, NULL, NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name);
