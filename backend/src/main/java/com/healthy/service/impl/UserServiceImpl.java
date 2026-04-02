package com.healthy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthy.entity.User;
import com.healthy.mapper.UserMapper;
import com.healthy.service.UserService;
import com.healthy.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public User findByAccount(String account) {
        try {
            return userMapper.selectOne(new QueryWrapper<User>()
                    .select("id", "account", "password", "name", "student_id", "gender", "role", "verified", "created_at", "updated_at")
                    .eq("account", account)
                    .last("LIMIT 1"));
        } catch (Exception ex) {
            logger.error("Error querying user by account {}", account, ex);
            return null;
        }
    }

    @Override
    public User findById(Long id) {
        if (id == null) return null;
        try {
            return userMapper.selectOne(new QueryWrapper<User>()
                    .select("id", "account", "password", "name", "student_id", "gender", "role", "verified", "created_at", "updated_at")
                    .eq("id", id)
                    .last("LIMIT 1"));
        } catch (Exception ex) {
            logger.error("Error querying user by id {}", id, ex);
            return null;
        }
    }

    @Override
    public User register(User user) {
        // check exists
        User exist = findByAccount(user.getAccount());
        if (exist != null) return null;
        user.setPassword(encoder.encode(user.getPassword()));
        if (user.getRole() == null) user.setRole("student");
        if (user.getVerified() == null) user.setVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        int r = userMapper.insert(user);
        return r > 0 ? user : null;
    }

    @Override
    public String login(String account, String rawPassword, String role) {
        try {
            User u = findByAccount(account);
            if (u == null) {
                logger.warn("Login failed: user not found for account={}", account);
                return null;
            }
            if (u.getPassword() == null) {
                logger.warn("Login failed: user has no password set account={}", account);
                return null;
            }
            boolean passwordOk = false;
            try {
                passwordOk = encoder.matches(rawPassword, u.getPassword());
            } catch (Exception ignored) {
                passwordOk = false;
            }
            // compatibility with legacy demo data that may store plain-text passwords
            if (!passwordOk) {
                passwordOk = rawPassword != null && rawPassword.equals(u.getPassword());
            }
            if (!passwordOk) {
                logger.warn("Login failed: bad credentials for account={}", account);
                return null;
            }
            if (role != null && !role.isEmpty() && !role.equals(u.getRole())) {
                logger.warn("Login failed: role mismatch for account={}, expected={}, got={}", account, role, u.getRole());
                return null;
            }
            return JwtUtil.generateToken(u);
        } catch (Exception ex) {
            logger.error("Unexpected error during login for account={}", account, ex);
            return null;
        }
    }

    @Override
    public boolean resetPassword(String account, String newPassword) {
        User u = findByAccount(account);
        if (u == null) return false;
        u.setPassword(encoder.encode(newPassword));
        u.setUpdatedAt(LocalDateTime.now());
        return userMapper.updateById(u) > 0;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User u = findById(userId);
        if (u == null || u.getPassword() == null) return false;
        if (!encoder.matches(oldPassword, u.getPassword())) return false;
        u.setPassword(encoder.encode(newPassword));
        u.setUpdatedAt(LocalDateTime.now());
        return userMapper.updateById(u) > 0;
    }

    @Override
    public User updateProfile(Long userId, String nickname, String gender, String avatar) {
        User u = findById(userId);
        if (u == null) return null;
        if (nickname != null && !nickname.isBlank()) u.setName(nickname.trim());
        if (gender != null && !gender.isBlank()) u.setGender(gender.trim());
        if (avatar != null && !avatar.isBlank()) u.setAvatar(avatar.trim());
        u.setUpdatedAt(LocalDateTime.now());
        try {
            userMapper.updateById(u);
            return findById(userId);
        } catch (Exception ex) {
            // Some old schemas may not have avatar column. Retry without avatar.
            logger.warn("Profile update fallback without avatar column for userId={}", userId);
            try {
                u.setAvatar(null);
                userMapper.updateById(u);
                return findById(userId);
            } catch (Exception inner) {
                logger.error("Profile update failed for userId={}", userId, inner);
                return null;
            }
        }
    }

    @Override
    public boolean markVerifiedByAccount(String account) {
        User u = findByAccount(account);
        if (u == null) return false;
        u.setVerified(true);
        u.setUpdatedAt(LocalDateTime.now());
        return userMapper.updateById(u) > 0;
    }
}
