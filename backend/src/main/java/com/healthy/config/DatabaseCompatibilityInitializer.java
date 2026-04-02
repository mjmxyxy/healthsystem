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
    }

    private void dropForeignKeyIfExists(String tableName, String foreignKeyName) {
        try {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " DROP FOREIGN KEY " + foreignKeyName);
            logger.info("Dropped legacy foreign key {} from {} table", foreignKeyName, tableName);
        } catch (Exception ex) {
            logger.info("Foreign key {} on {} not dropped (likely already removed): {}", foreignKeyName, tableName, ex.getMessage());
        }
    }
}
