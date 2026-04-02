-- Measurement schema: scales, questions, reports
CREATE TABLE IF NOT EXISTS scales (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  description TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS questions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  scale_id BIGINT NOT NULL,
  seq INT NOT NULL,
  text TEXT NOT NULL,
  max_score INT DEFAULT 4,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (scale_id) REFERENCES scales(id) ON DELETE CASCADE
);

-- reports stores summary and full answers as JSON in details
CREATE TABLE IF NOT EXISTS reports (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  scale_id BIGINT NOT NULL,
  total_score DOUBLE,
  avg_score DOUBLE,
  level VARCHAR(32),
  details JSON,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (scale_id) REFERENCES scales(id) ON DELETE CASCADE
);

-- Seed SCL-90 sample scale and questions (partial sample)
INSERT INTO scales (code, name, description) VALUES ('SCL90', 'SCL-90', 'Symptom Checklist-90 (sample)');
SET @scaleId = LAST_INSERT_ID();

INSERT INTO questions (scale_id, seq, text, max_score) VALUES
(@scaleId, 1, '感到紧张或焦虑', 4),
(@scaleId, 2, '感到抑郁情绪', 4),
(@scaleId, 3, '睡眠不好或失眠', 4),
(@scaleId, 4, '注意力难以集中', 4),
(@scaleId, 5, '感觉自己没价值', 4),
(@scaleId, 6, '感到孤独', 4),
(@scaleId, 7, '食欲改变', 4),
(@scaleId, 8, '容易疲劳', 4),
(@scaleId, 9, '对事物失去兴趣', 4),
(@scaleId,10, '对未来感到悲观', 4),
(@scaleId,11, '心跳加速',4),
(@scaleId,12, '容易发怒',4),
(@scaleId,13, '感到恐惧',4),
(@scaleId,14, '体验到身体不适',4),
(@scaleId,15, '难以放松',4);
