package com.healthy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthy.entity.Article;
import com.healthy.entity.User;
import com.healthy.mapper.ArticleMapper;
import com.healthy.service.UserService;
import com.healthy.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleMapper articleMapper;

    @GetMapping("/articles")
    public ApiResponse<?> listArticles() {
        List<Article> rows = articleMapper.selectList(new QueryWrapper<Article>()
                .eq("status", 2)
                .orderByDesc("publish_time")
                .orderByDesc("create_time"));
        List<Map<String, Object>> data = rows.stream().map(this::toArticleVo).toList();
        return ApiResponse.ok(data);
    }

    @GetMapping("/articles/{id}")
    public ApiResponse<?> articleDetail(@PathVariable Long id) {
        Article row = articleMapper.selectById(id);
        if (row == null || row.getStatus() == null || row.getStatus() != 2) {
            return ApiResponse.error("article not found");
        }
        return ApiResponse.ok(toArticleVo(row));
    }

    private Map<String, Object> toArticleVo(Article row) {
        String authorName = "咨询师";
        if (row.getAuthorId() != null) {
            User author = userService.findById(row.getAuthorId());
            if (author != null && author.getName() != null && !author.getName().isBlank()) {
                authorName = author.getName();
            }
        }
        Map<String, Object> m = new HashMap<>();
        m.put("id", row.getId());
        m.put("title", row.getTitle());
        m.put("content", row.getContent());
        m.put("cover", row.getCover());
        m.put("authorId", row.getAuthorId());
        m.put("authorName", authorName);
        m.put("publishTime", row.getPublishTime());
        m.put("createTime", row.getCreateTime());
        return m;
    }

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
