package com.healthy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class DatabaseCompatibilityInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseCompatibilityInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        dropForeignKeyIfExists("assessment", "assessment_ibfk_1");
        dropForeignKeyIfExists("appointment", "appointment_ibfk_1");
        dropForeignKeyIfExists("appointment", "appointment_ibfk_2");
        dropForeignKeyIfExists("article", "article_ibfk_1");
        dropForeignKeyIfExists("warning", "warning_ibfk_1");
        dropForeignKeyIfExists("warning", "warning_ibfk_2");
        dropForeignKeyIfExists("notification", "notification_ibfk_1");

        createTableIfMissing("""
                CREATE TABLE IF NOT EXISTS counselor_student_note (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    counselor_id BIGINT NOT NULL,
                    student_id BIGINT NOT NULL,
                    note TEXT,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_counselor_student (counselor_id, student_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

        createTableIfMissing("""
                CREATE TABLE IF NOT EXISTS counselor_report_note (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    counselor_id BIGINT NOT NULL,
                    report_id BIGINT NOT NULL,
                    analysis TEXT,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_counselor_report (counselor_id, report_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

        createTableIfMissing("""
                CREATE TABLE IF NOT EXISTS admin_user_state (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    disabled TINYINT NOT NULL DEFAULT 0,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_admin_user_state_user (user_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
    }

    private void dropForeignKeyIfExists(String tableName, String foreignKeyName) {
        try {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " DROP FOREIGN KEY " + foreignKeyName);
            logger.info("Dropped legacy foreign key {} from {} table", foreignKeyName, tableName);
        } catch (Exception ex) {
            logger.info("Foreign key {} on {} not dropped (likely already removed): {}", foreignKeyName, tableName, ex.getMessage());
        }
    }

    private void createTableIfMissing(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ex) {
            logger.warn("Create table failed: {}", ex.getMessage());
        }
    }
}
