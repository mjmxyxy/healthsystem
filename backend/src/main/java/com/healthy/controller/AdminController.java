package com.healthy.controller;

import com.healthy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private UserService userService;

    // Mark a student's account as verified. Requires authenticated request (admin role enforcement can be applied client-side or via roles.)
    @PostMapping("/verify-student")
    public ResponseEntity<?> verifyStudent(@RequestBody Map<String, String> body) {
        String account = body.getOrDefault("account", "");
        if (account.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "account required"));
        boolean ok = userService.markVerifiedByAccount(account);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("error", "account not found"));
        return ResponseEntity.ok(Map.of("message", "verified"));
    }
}
