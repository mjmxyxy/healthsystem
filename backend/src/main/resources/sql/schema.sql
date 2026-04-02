-- MySQL schema for campus counseling platform (updated per spec)

CREATE DATABASE IF NOT EXISTS healthy_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE healthy_db;

-- 1. 用户表 (user)
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(128) NOT NULL,
  gender TINYINT NOT NULL COMMENT '0=未知/0,1=男,2=女',
  phone VARCHAR(32) DEFAULT NULL,
  avatar VARCHAR(256) DEFAULT NULL,
  role TINYINT NOT NULL COMMENT '1=学生,2=咨询师,3=管理员',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用,1=正常',
  name VARCHAR(128) DEFAULT NULL,
  student_id VARCHAR(64) DEFAULT NULL,
  email VARCHAR(128) DEFAULT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_username (username),
  INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 咨询师表 (counselor)
DROP TABLE IF EXISTS counselor;
CREATE TABLE counselor (
  id INT PRIMARY KEY, -- 关联 user.id
  expertise VARCHAR(256) DEFAULT NULL,
  introduction TEXT DEFAULT NULL,
  rating DECIMAL(3,2) DEFAULT NULL COMMENT '综合评分 1.00-5.00',
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '元/小时',
  style VARCHAR(128) DEFAULT NULL,
  schedule JSON DEFAULT NULL,
  audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '0=未提交/1=待审核/2=通过/3=拒绝',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 预约表 (appointment)
DROP TABLE IF EXISTS appointment;
CREATE TABLE appointment (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  counselor_id INT NOT NULL,
  appt_time DATETIME NOT NULL,
  duration INT NOT NULL COMMENT '分钟',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0=待确认,1=已确认,2=已完成,3=已取消',
  note VARCHAR(512) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES `user`(id),
  FOREIGN KEY (counselor_id) REFERENCES `user`(id),
  INDEX idx_counselor_time (counselor_id, appt_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 咨询记录表 (consultation)
DROP TABLE IF EXISTS consultation;
CREATE TABLE consultation (
  id INT AUTO_INCREMENT PRIMARY KEY,
  appointment_id INT NOT NULL,
  user_id INT NOT NULL,
  counselor_id INT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME DEFAULT NULL,
  summary TEXT DEFAULT NULL,
  rating TINYINT DEFAULT NULL COMMENT '1-5',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (appointment_id) REFERENCES appointment(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 心理量表表 (scale)
DROP TABLE IF EXISTS scale;
CREATE TABLE scale (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  code VARCHAR(64) NOT NULL,
  description TEXT DEFAULT NULL,
  question_count INT NOT NULL DEFAULT 0,
  dimensions JSON DEFAULT NULL,
  scoring_rules JSON DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 量表题目表 (scale_question)
DROP TABLE IF EXISTS scale_question;
CREATE TABLE scale_question (
  id INT AUTO_INCREMENT PRIMARY KEY,
  scale_id INT NOT NULL,
  question_no INT NOT NULL,
  content VARCHAR(1024) NOT NULL,
  options JSON NOT NULL,
  reverse_score TINYINT NOT NULL DEFAULT 0,
  dimension VARCHAR(128) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (scale_id) REFERENCES scale(id),
  INDEX idx_scale_qno (scale_id, question_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 测评结果表 (assessment)
DROP TABLE IF EXISTS assessment;
CREATE TABLE assessment (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  scale_id INT NOT NULL,
  dimension_scores JSON NOT NULL,
  total_score FLOAT NOT NULL,
  standard_score FLOAT DEFAULT NULL,
  level TINYINT NOT NULL COMMENT '0=正常,1=轻度,2=中度,3=重度',
  level_desc VARCHAR(64) NOT NULL,
  report TEXT NOT NULL,
  answers JSON NOT NULL,
  test_time DATETIME NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES `user`(id),
  FOREIGN KEY (scale_id) REFERENCES scale(id),
  INDEX idx_user_testtime (user_id, test_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. 文章表 (article)
DROP TABLE IF EXISTS article;
CREATE TABLE article (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(256) NOT NULL,
  content LONGTEXT NOT NULL,
  cover VARCHAR(256) DEFAULT NULL,
  author_id INT NOT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0=draft,1=pending,2=published,3=rejected',
  publish_time DATETIME DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (author_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. 预警表 (warning)
DROP TABLE IF EXISTS warning;
CREATE TABLE warning (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  level TINYINT NOT NULL COMMENT '风险等级',
  assessment_id INT DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0=未处理,1=处理中,2=已处理',
  handler_id INT DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES `user`(id),
  FOREIGN KEY (assessment_id) REFERENCES assessment(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. 消息通知表 (notification)
DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  type TINYINT NOT NULL COMMENT '通知类型',
  title VARCHAR(256) NOT NULL,
  content VARCHAR(1024) NOT NULL,
  is_read TINYINT NOT NULL DEFAULT 0,
  target_url VARCHAR(512) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES `user`(id),
  INDEX idx_user_unread (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
