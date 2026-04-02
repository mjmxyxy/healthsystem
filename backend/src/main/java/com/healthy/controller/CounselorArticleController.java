package com.healthy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthy.entity.Article;
import com.healthy.mapper.ArticleMapper;
import com.healthy.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/counselor/articles")
@CrossOrigin(origins = "http://localhost:5173")
public class CounselorArticleController {

    @Autowired
    private ArticleMapper articleMapper;

    private Long currentCounselorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalArgumentException("未登录");
        }
        boolean counselor = false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if ("ROLE_COUNSELOR".equals(ga.getAuthority())) {
                counselor = true;
                break;
            }
        }
        if (!counselor) {
            throw new IllegalArgumentException("无咨询师权限");
        }
        return Long.parseLong(String.valueOf(auth.getPrincipal()));
    }

    private Map<String, Object> toVo(Article a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("title", a.getTitle());
        m.put("content", a.getContent());
        m.put("cover", a.getCover());
        m.put("authorId", a.getAuthorId());
        m.put("status", a.getStatus());
        m.put("publishTime", a.getPublishTime());
        m.put("createTime", a.getCreateTime());
        m.put("statusText", switch (a.getStatus() == null ? 0 : a.getStatus()) {
            case 0 -> "草稿";
            case 1 -> "待审核";
            case 2 -> "已发布";
            case 3 -> "已拒绝";
            default -> "未知";
        });
        return m;
    }

    @GetMapping
    public ApiResponse<?> list() {
        Long counselorId = currentCounselorId();
        List<Article> list = articleMapper.selectList(new QueryWrapper<Article>()
                .eq("author_id", counselorId)
                .orderByDesc("create_time"));
        return ApiResponse.ok(list.stream().map(this::toVo).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        Long counselorId = currentCounselorId();
        Article a = articleMapper.selectById(id);
        if (a == null) return ApiResponse.error("article not found");
        if (!counselorId.equals(a.getAuthorId())) throw new IllegalArgumentException("forbidden");
        return ApiResponse.ok(toVo(a));
    }

    @PostMapping
    public ApiResponse<?> save(@RequestBody Map<String, Object> body) {
        Long counselorId = currentCounselorId();
        String title = String.valueOf(body.getOrDefault("title", "")).trim();
        String content = String.valueOf(body.getOrDefault("content", "")).trim();
        String cover = String.valueOf(body.getOrDefault("cover", "")).trim();
        Integer status = Integer.parseInt(String.valueOf(body.getOrDefault("status", "0")));
        // Counselor can only save draft(0) or submit for review(1). Publishing(2) is admin-only.
        if (status != 0) {
            status = 1;
        }
        Long id = body.get("id") == null ? null : Long.parseLong(String.valueOf(body.get("id")));
        if (title.isBlank() || content.isBlank()) return ApiResponse.error("title/content required");

        Article row = id == null ? new Article() : articleMapper.selectById(id);
        if (row != null && row.getAuthorId() != null && !counselorId.equals(row.getAuthorId())) {
            throw new IllegalArgumentException("forbidden");
        }
        if (row == null) {
            row = new Article();
            row.setCreateTime(LocalDateTime.now());
        }
        row.setAuthorId(counselorId);
        row.setTitle(title);
        row.setContent(content);
        row.setCover(cover);
        row.setStatus(status);
        if (status != 2) row.setPublishTime(null);
        if (row.getId() == null) {
            articleMapper.insert(row);
        } else {
            articleMapper.updateById(row);
        }
        return ApiResponse.ok(toVo(row));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<?> publish(@PathVariable Long id) {
        Long counselorId = currentCounselorId();
        Article a = articleMapper.selectById(id);
        if (a == null) return ApiResponse.error("article not found");
        if (!counselorId.equals(a.getAuthorId())) throw new IllegalArgumentException("forbidden");
        // Keep compatibility with old frontend action: treat as submit-for-review.
        a.setStatus(1);
        a.setPublishTime(null);
        articleMapper.updateById(a);
        return ApiResponse.ok(toVo(a));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        Long counselorId = currentCounselorId();
        Article a = articleMapper.selectById(id);
        if (a == null) return ApiResponse.error("article not found");
        if (!counselorId.equals(a.getAuthorId())) throw new IllegalArgumentException("forbidden");
        articleMapper.deleteById(id);
        return ApiResponse.ok(Map.of("message", "deleted"));
    }
}
