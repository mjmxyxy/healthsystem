package com.healthy.controller;

import com.healthy.entity.User;
import com.healthy.service.UserService;
import com.healthy.util.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String account = body.getOrDefault("account", "");
        String password = body.getOrDefault("password", "");
        String name = body.getOrDefault("name", "");
        String studentId = body.getOrDefault("studentId", "");
        String gender = body.getOrDefault("gender", "");
        String role = body.getOrDefault("role", "student");
        if (account.isEmpty() || password.isEmpty() || name.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing required fields"));
        }
        User u = new User();
        u.setAccount(account);
        u.setPassword(password);
        u.setName(name);
        u.setStudentId(studentId);
        u.setGender(gender);
        u.setRole(role);
        u.setVerified(false);
        User created = userService.register(u);
        if (created == null) return ResponseEntity.badRequest().body(Map.of("error", "account exists"));
        return ResponseEntity.ok(Map.of("message", "registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String account = body.getOrDefault("account", "");
        String password = body.getOrDefault("password", "");
        String role = body.getOrDefault("role", "");
        if (account.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "account/password required"));
        }
        String token = userService.login(account, password, role);
        if (token == null) return ResponseEntity.status(401).body(Map.of("error", "invalid credentials or role"));
        User user = userService.findByAccount(account);
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("role", role.isEmpty() && user != null ? user.getRole() : role);
        if (user != null) {
            resp.put("userId", user.getId());
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody Map<String, String> body) {
        String account = body.getOrDefault("account", "");
        String newPassword = body.getOrDefault("newPassword", "");
        if (account.isEmpty() || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "account and newPassword required"));
        }
        boolean ok = userService.resetPassword(account, newPassword);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("error", "account not found"));
        return ResponseEntity.ok(Map.of("message", "password reset"));
    }

    @GetMapping("/aes-test")
    public ResponseEntity<String> aesTest(@RequestParam String text) throws Exception {
        String key = System.getenv().getOrDefault("AES_KEY", "change_this_key_32_bytes_long");
        String enc = AESUtil.encrypt(text, key);
        String dec = AESUtil.decrypt(enc, key);
        return ResponseEntity.ok("enc=" + enc + " dec=" + dec);
    }
}
