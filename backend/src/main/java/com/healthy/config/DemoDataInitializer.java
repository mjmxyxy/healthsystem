package com.healthy.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthy.entity.User;
import com.healthy.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(1)
public class DemoDataInitializer implements ApplicationRunner {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public void run(ApplicationArguments args) {
        ensureDemoUser("student_demo", "123456", "学生演示账号", "student");
        ensureDemoUser("counselor_demo", "123456", "咨询师演示账号", "counselor");
    }

    private void ensureDemoUser(String account, String password, String name, String role) {
        User exist = userMapper.selectOne(new QueryWrapper<User>().eq("account", account));
        if (exist != null) return;

        User u = new User();
        u.setAccount(account);
        u.setPassword(encoder.encode(password));
        u.setName(name);
        u.setRole(role);
        u.setVerified(false);
        u.setStudentId(role.equals("student") ? "20260001" : null);
        u.setGender("未知");
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(u);
    }
}
