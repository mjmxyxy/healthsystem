package com.healthy.service;

import com.healthy.entity.User;

public interface UserService {
    User findByAccount(String account);
    User findById(Long id);
    User register(User user);
    String login(String account, String rawPassword, String role);
    boolean resetPassword(String account, String newPassword);
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    User updateProfile(Long userId, String nickname, String gender, String avatar);
    boolean markVerifiedByAccount(String account);
}
