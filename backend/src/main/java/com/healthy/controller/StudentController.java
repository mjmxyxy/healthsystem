package com.healthy.controller;

import com.healthy.entity.User;
import com.healthy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestParam(required = false) Long userId, Principal principal) {
        Long id = userId;
        if (id == null && principal != null) {
            try { id = Long.parseLong(principal.getName()); } catch (Exception ignored) {}
        }
        if (id == null || id <= 0) return ResponseEntity.badRequest().body(Map.of("error", "userId required"));
        User u = userService.findById(id);
        if (u == null) return ResponseEntity.badRequest().body(Map.of("error", "user not found"));
        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "account", u.getAccount(),
                "nickname", u.getName() == null ? "" : u.getName(),
                "gender", u.getGender() == null ? "" : u.getGender(),
                "avatar", u.getAvatar() == null ? "" : u.getAvatar(),
                "role", u.getRole() == null ? "student" : u.getRole()
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, Principal principal) {
        Long id = null;
        try {
            if (body.get("userId") != null) id = Long.parseLong(body.get("userId"));
            else if (principal != null) id = Long.parseLong(principal.getName());
        } catch (Exception ignored) {}
        if (id == null || id <= 0) return ResponseEntity.badRequest().body(Map.of("error", "userId required"));

        String nickname = body.getOrDefault("nickname", "");
        String gender = body.getOrDefault("gender", "");
        String avatar = body.getOrDefault("avatar", "");
        User updated = userService.updateProfile(id, nickname, gender, avatar);
        if (updated == null) return ResponseEntity.badRequest().body(Map.of("error", "update failed"));
        return ResponseEntity.ok(Map.of(
                "id", updated.getId(),
                "nickname", updated.getName() == null ? "" : updated.getName(),
                "gender", updated.getGender() == null ? "" : updated.getGender(),
                "avatar", updated.getAvatar() == null ? "" : updated.getAvatar()
        ));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, Principal principal) {
        Long id = null;
        try {
            if (body.get("userId") != null) id = Long.parseLong(body.get("userId"));
            else if (principal != null) id = Long.parseLong(principal.getName());
        } catch (Exception ignored) {}
        String oldPassword = body.getOrDefault("oldPassword", "");
        String newPassword = body.getOrDefault("newPassword", "");
        if (id == null || id <= 0) return ResponseEntity.badRequest().body(Map.of("error", "userId required"));
        if (oldPassword.isBlank() || newPassword.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "oldPassword/newPassword required"));
        boolean ok = userService.changePassword(id, oldPassword, newPassword);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("error", "原密码错误或修改失败"));
        return ResponseEntity.ok(Map.of("success", true));
    }
}
