package com.healthy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthy.entity.AdminUserState;
import com.healthy.entity.AppointmentRecord;
import com.healthy.entity.Article;
import com.healthy.entity.Question;
import com.healthy.entity.Report;
import com.healthy.entity.Scale;
import com.healthy.entity.User;
import com.healthy.mapper.AdminUserStateMapper;
import com.healthy.mapper.AppointmentMapper;
import com.healthy.mapper.ArticleMapper;
import com.healthy.mapper.QuestionMapper;
import com.healthy.mapper.ReportMapper;
import com.healthy.mapper.ScaleMapper;
import com.healthy.mapper.UserMapper;
import com.healthy.service.UserService;
import com.healthy.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/admin", "/admin"})
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ScaleMapper scaleMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private AdminUserStateMapper adminUserStateMapper;

    private Long currentAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalArgumentException("未登录");
        }
        boolean admin = false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if ("ROLE_ADMIN".equals(ga.getAuthority())) {
                admin = true;
                break;
            }
        }
        if (!admin) {
            throw new IllegalArgumentException("无管理员权限");
        }
        return Long.parseLong(String.valueOf(auth.getPrincipal()));
    }

    private int disabledOf(Long userId) {
        AdminUserState s = adminUserStateMapper.selectOne(new QueryWrapper<AdminUserState>().eq("user_id", userId));
        return (s == null || s.getDisabled() == null) ? 0 : s.getDisabled();
    }

    private void setDisabled(Long userId, int disabled) {
        AdminUserState s = adminUserStateMapper.selectOne(new QueryWrapper<AdminUserState>().eq("user_id", userId));
        if (s == null) {
            s = new AdminUserState();
            s.setUserId(userId);
            s.setDisabled(disabled);
            s.setUpdatedAt(LocalDateTime.now());
            adminUserStateMapper.insert(s);
        } else {
            s.setDisabled(disabled);
            s.setUpdatedAt(LocalDateTime.now());
            adminUserStateMapper.updateById(s);
        }
    }

    @GetMapping("/dashboard")
    public ApiResponse<?> dashboard() {
        currentAdminId();
        long students = userMapper.selectCount(new QueryWrapper<User>().eq("role", "student"));
        long counselors = userMapper.selectCount(new QueryWrapper<User>().eq("role", "counselor"));
        long assessments = reportMapper.selectCount(new QueryWrapper<Report>());
        long appointments = appointmentMapper.selectCount(new QueryWrapper<AppointmentRecord>());
        long pendingArticles = articleMapper.selectCount(new QueryWrapper<Article>().eq("status", 1));
        Map<String, Object> data = new HashMap<>();
        data.put("studentCount", students);
        data.put("counselorCount", counselors);
        data.put("assessmentCount", assessments);
        data.put("appointmentCount", appointments);
        data.put("pendingArticleCount", pendingArticles);
        return ApiResponse.ok(data);
    }

    @GetMapping("/students")
    public ApiResponse<?> students(@RequestParam(required = false) String keyword) {
        currentAdminId();
        QueryWrapper<User> qw = new QueryWrapper<User>().eq("role", "student").orderByDesc("created_at");
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("account", keyword).or().like("name", keyword).or().like("student_id", keyword));
        }
        List<User> rows = userMapper.selectList(qw);
        List<Map<String, Object>> data = rows.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("account", u.getAccount());
            m.put("name", u.getName());
            m.put("studentId", u.getStudentId());
            m.put("gender", u.getGender());
            m.put("verified", u.getVerified());
            m.put("disabled", disabledOf(u.getId()));
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).toList();
        return ApiResponse.ok(data);
    }

    @GetMapping("/students/{id}")
    public ApiResponse<?> studentDetail(@PathVariable Long id) {
        currentAdminId();
        User u = userMapper.selectByIdSafe(id);
        if (u == null || !"student".equals(u.getRole())) return ApiResponse.error("student not found");
        Map<String, Object> data = new HashMap<>();
        data.put("id", u.getId());
        data.put("account", u.getAccount());
        data.put("name", u.getName());
        data.put("studentId", u.getStudentId());
        data.put("gender", u.getGender());
        data.put("verified", u.getVerified());
        data.put("disabled", disabledOf(u.getId()));
        data.put("createdAt", u.getCreatedAt());
        return ApiResponse.ok(data);
    }

    @PostMapping("/students/{id}/disable")
    public ApiResponse<?> disableStudent(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        currentAdminId();
        User u = userMapper.selectByIdSafe(id);
        if (u == null || !"student".equals(u.getRole())) return ApiResponse.error("student not found");
        int disabled = Integer.parseInt(String.valueOf(body.getOrDefault("disabled", "1")));
        setDisabled(id, disabled == 1 ? 1 : 0);
        return ApiResponse.ok(Map.of("id", id, "disabled", disabled == 1 ? 1 : 0));
    }

    @GetMapping("/students/export")
    public ResponseEntity<byte[]> exportStudents() {
        currentAdminId();
        List<User> rows = userMapper.selectList(new QueryWrapper<User>().eq("role", "student").orderByDesc("created_at"));
        StringBuilder sb = new StringBuilder();
        sb.append("id,account,name,studentId,gender,verified,disabled,createdAt\n");
        for (User u : rows) {
            sb.append(u.getId()).append(',')
                    .append(csv(u.getAccount())).append(',')
                    .append(csv(u.getName())).append(',')
                    .append(csv(u.getStudentId())).append(',')
                    .append(csv(u.getGender())).append(',')
                    .append(Boolean.TRUE.equals(u.getVerified()) ? "1" : "0").append(',')
                    .append(disabledOf(u.getId())).append(',')
                    .append(csv(String.valueOf(u.getCreatedAt())))
                    .append('\n');
        }
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=utf-8")
                .header("Content-Disposition", "attachment; filename=students.csv")
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/counselors")
    public ApiResponse<?> counselors(@RequestParam(required = false) String keyword) {
        currentAdminId();
        QueryWrapper<User> qw = new QueryWrapper<User>().eq("role", "counselor").orderByDesc("created_at");
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("account", keyword).or().like("name", keyword));
        }
        List<User> rows = userMapper.selectList(qw);
        List<Map<String, Object>> data = rows.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("account", u.getAccount() == null ? "" : u.getAccount());
            m.put("name", u.getName() == null ? "" : u.getName());
            m.put("gender", u.getGender() == null ? "" : u.getGender());
            m.put("verified", Boolean.TRUE.equals(u.getVerified()) ? 1 : 0);
            m.put("disabled", disabledOf(u.getId()));
            m.put("createdAt", u.getCreatedAt() == null ? "" : String.valueOf(u.getCreatedAt()));
            return m;
        }).toList();
        return ApiResponse.ok(data);
    }

    @PostMapping("/counselors/{id}/review")
    public ApiResponse<?> reviewCounselor(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        currentAdminId();
        User u = userMapper.selectByIdSafe(id);
        if (u == null || !"counselor".equals(u.getRole())) return ApiResponse.error("counselor not found");
        int approved = Integer.parseInt(String.valueOf(body.getOrDefault("approved", "1")));
        u.setVerified(approved == 1);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
        return ApiResponse.ok(Map.of("id", id, "verified", approved == 1 ? 1 : 0));
    }

    @PostMapping("/counselors/{id}/shelve")
    public ApiResponse<?> shelveCounselor(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        currentAdminId();
        User u = userMapper.selectByIdSafe(id);
        if (u == null || !"counselor".equals(u.getRole())) return ApiResponse.error("counselor not found");
        int disabled = Integer.parseInt(String.valueOf(body.getOrDefault("disabled", "0")));
        setDisabled(id, disabled == 1 ? 1 : 0);
        return ApiResponse.ok(Map.of("id", id, "disabled", disabled == 1 ? 1 : 0));
    }

    @PutMapping("/counselors/{id}")
    public ApiResponse<?> editCounselor(@PathVariable Long id, @RequestBody Map<String, String> body) {
        currentAdminId();
        User u = userMapper.selectByIdSafe(id);
        if (u == null || !"counselor".equals(u.getRole())) return ApiResponse.error("counselor not found");
        String name = body.getOrDefault("name", "").trim();
        String gender = body.getOrDefault("gender", "").trim();
        if (!name.isBlank()) u.setName(name);
        if (!gender.isBlank()) u.setGender(gender);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
        return ApiResponse.ok(Map.of("id", u.getId(), "name", u.getName(), "gender", u.getGender()));
    }

    @GetMapping("/scales")
    public ApiResponse<?> scales() {
        currentAdminId();
        List<Scale> rows = scaleMapper.selectList(new QueryWrapper<Scale>().orderByDesc("create_time"));
        List<Map<String, Object>> data = rows.stream().map(s -> {
            long qCount = questionMapper.selectCount(new QueryWrapper<Question>().eq("scale_id", s.getId()));
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("code", s.getCode());
            m.put("name", s.getName());
            m.put("description", s.getDescription());
            m.put("questionCount", qCount);
            m.put("createTime", s.getCreateTime());
            return m;
        }).toList();
        return ApiResponse.ok(data);
    }

    @PostMapping("/scales")
    public ApiResponse<?> saveScale(@RequestBody Map<String, Object> body) {
        currentAdminId();
        Long id = body.get("id") == null ? null : Long.parseLong(String.valueOf(body.get("id")));
        String code = String.valueOf(body.getOrDefault("code", "")).trim();
        String name = String.valueOf(body.getOrDefault("name", "")).trim();
        String description = String.valueOf(body.getOrDefault("description", "")).trim();
        if (code.isBlank() || name.isBlank()) return ApiResponse.error("code/name required");
        Scale s = id == null ? new Scale() : scaleMapper.selectById(id);
        if (s == null) s = new Scale();
        s.setCode(code);
        s.setName(name);
        s.setDescription(description);
        if (s.getId() == null) {
            s.setCreateTime(LocalDateTime.now());
            scaleMapper.insert(s);
        } else {
            scaleMapper.updateById(s);
        }
        return ApiResponse.ok(s);
    }

    @DeleteMapping("/scales/{id}")
    public ApiResponse<?> deleteScale(@PathVariable Long id) {
        currentAdminId();
        questionMapper.delete(new QueryWrapper<Question>().eq("scale_id", id));
        scaleMapper.deleteById(id);
        return ApiResponse.ok(Map.of("deleted", id));
    }

    @GetMapping("/scales/{id}/questions")
    public ApiResponse<?> questions(@PathVariable Long id) {
        currentAdminId();
        List<Question> rows = questionMapper.selectList(new QueryWrapper<Question>().eq("scale_id", id).orderByAsc("question_no"));
        return ApiResponse.ok(rows);
    }

    @PostMapping("/scales/{id}/questions")
    public ApiResponse<?> addQuestion(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        currentAdminId();
        Question q = new Question();
        q.setScaleId(id);
        q.setSeq(Integer.parseInt(String.valueOf(body.getOrDefault("seq", "1"))));
        q.setText(String.valueOf(body.getOrDefault("text", "")).trim());
        q.setOptions(String.valueOf(body.getOrDefault("options", "[]")));
        q.setReverseScore(Integer.parseInt(String.valueOf(body.getOrDefault("reverseScore", "0"))));
        q.setDimension(String.valueOf(body.getOrDefault("dimension", "")).trim());
        q.setCreatedAt(LocalDateTime.now());
        questionMapper.insert(q);
        refreshQuestionCount(id);
        return ApiResponse.ok(q);
    }

    @PutMapping("/questions/{id}")
    public ApiResponse<?> updateQuestion(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        currentAdminId();
        Question q = questionMapper.selectById(id);
        if (q == null) return ApiResponse.error("question not found");
        q.setSeq(Integer.parseInt(String.valueOf(body.getOrDefault("seq", q.getSeq()))));
        q.setText(String.valueOf(body.getOrDefault("text", q.getText())));
        q.setOptions(String.valueOf(body.getOrDefault("options", q.getOptions())));
        q.setReverseScore(Integer.parseInt(String.valueOf(body.getOrDefault("reverseScore", q.getReverseScore() == null ? 0 : q.getReverseScore()))));
        q.setDimension(String.valueOf(body.getOrDefault("dimension", q.getDimension() == null ? "" : q.getDimension())));
        questionMapper.updateById(q);
        refreshQuestionCount(q.getScaleId());
        return ApiResponse.ok(q);
    }

    @DeleteMapping("/questions/{id}")
    public ApiResponse<?> deleteQuestion(@PathVariable Long id) {
        currentAdminId();
        Question q = questionMapper.selectById(id);
        if (q == null) return ApiResponse.error("question not found");
        questionMapper.deleteById(id);
        refreshQuestionCount(q.getScaleId());
        return ApiResponse.ok(Map.of("deleted", id));
    }

    @GetMapping("/appointments")
    public ApiResponse<?> appointments(@RequestParam(required = false) Integer status) {
        currentAdminId();
        QueryWrapper<AppointmentRecord> qw = new QueryWrapper<AppointmentRecord>().orderByDesc("create_time");
        if (status != null) qw.eq("status", status);
        List<AppointmentRecord> rows = appointmentMapper.selectList(qw);
        List<Long> uids = rows.stream().flatMap(r -> List.of(r.getUserId(), r.getCounselorId()).stream()).distinct().collect(Collectors.toList());
        Map<Long, User> users = uids.isEmpty() ? Map.of() : userMapper.selectBatchIds(uids).stream().collect(Collectors.toMap(User::getId, u -> u));
        List<Map<String, Object>> data = rows.stream().map(r -> {
            User su = users.get(r.getUserId());
            User cu = users.get(r.getCounselorId());
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("studentId", r.getUserId());
            m.put("studentName", su == null ? "" : su.getName());
            m.put("counselorId", r.getCounselorId());
            m.put("counselorName", cu == null ? "" : cu.getName());
            m.put("apptTime", r.getApptTime());
            m.put("duration", r.getDuration());
            m.put("status", r.getStatus());
            m.put("note", r.getNote());
            m.put("createTime", r.getCreateTime());
            return m;
        }).toList();
        return ApiResponse.ok(data);
    }

    @GetMapping("/articles")
    public ApiResponse<?> articles(@RequestParam(required = false) Integer status) {
        currentAdminId();
        QueryWrapper<Article> qw = new QueryWrapper<Article>().orderByDesc("create_time");
        if (status != null) qw.eq("status", status);
        List<Article> rows = articleMapper.selectList(qw);
        List<Long> authorIds = rows.stream().map(Article::getAuthorId).filter(a -> a != null).distinct().toList();
        Map<Long, User> users = authorIds.isEmpty() ? Map.of() : userMapper.selectBatchIds(authorIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        List<Map<String, Object>> data = rows.stream().map(a -> {
            User au = users.get(a.getAuthorId());
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("title", a.getTitle());
            m.put("content", a.getContent());
            m.put("cover", a.getCover());
            m.put("authorId", a.getAuthorId());
            m.put("authorName", au == null ? "" : au.getName());
            m.put("status", a.getStatus());
            m.put("statusText", switch (a.getStatus() == null ? 0 : a.getStatus()) {
                case 0 -> "草稿";
                case 1 -> "待审核";
                case 2 -> "已发布";
                case 3 -> "已拒绝";
                default -> "未知";
            });
            m.put("publishTime", a.getPublishTime());
            m.put("createTime", a.getCreateTime());
            return m;
        }).toList();
        return ApiResponse.ok(data);
    }

    @PostMapping("/articles/{id}/review")
    public ApiResponse<?> reviewArticle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        currentAdminId();
        Article a = articleMapper.selectById(id);
        if (a == null) return ApiResponse.error("article not found");
        int approved = Integer.parseInt(String.valueOf(body.getOrDefault("approved", "1")));
        if (approved == 1) {
            a.setStatus(2);
            a.setPublishTime(LocalDateTime.now());
        } else {
            a.setStatus(3);
        }
        articleMapper.updateById(a);
        return ApiResponse.ok(Map.of("id", id, "status", a.getStatus()));
    }

    @GetMapping("/profile")
    public ApiResponse<?> profile() {
        Long id = currentAdminId();
        User u = userService.findById(id);
        if (u == null) return ApiResponse.error("user not found");
        return ApiResponse.ok(Map.of(
                "id", u.getId(),
                "account", u.getAccount() == null ? "" : u.getAccount(),
                "name", u.getName() == null ? "" : u.getName(),
                "gender", u.getGender() == null ? "" : u.getGender(),
                "role", u.getRole() == null ? "admin" : u.getRole()
        ));
    }

    @PutMapping("/profile")
    public ApiResponse<?> updateProfile(@RequestBody Map<String, String> body) {
        Long id = currentAdminId();
        User u = userService.updateProfile(id, body.getOrDefault("name", ""), body.getOrDefault("gender", ""), "");
        if (u == null) return ApiResponse.error("update failed");
        return ApiResponse.ok(Map.of("id", u.getId(), "name", u.getName(), "gender", u.getGender()));
    }

    @PutMapping("/password")
    public ApiResponse<?> changePassword(@RequestBody Map<String, String> body) {
        Long id = currentAdminId();
        String oldPassword = body.getOrDefault("oldPassword", "");
        String newPassword = body.getOrDefault("newPassword", "");
        if (oldPassword.isBlank() || newPassword.isBlank()) return ApiResponse.error("old/new password required");
        boolean ok = userService.changePassword(id, oldPassword, newPassword);
        if (!ok) return ApiResponse.error("原密码错误或修改失败");
        return ApiResponse.ok(Map.of("success", true));
    }

    // Mark a student's account as verified. Requires authenticated request (admin role enforcement can be applied client-side or via roles.)
    @PostMapping("/verify-student")
    public ApiResponse<?> verifyStudent(@RequestBody Map<String, String> body) {
        currentAdminId();
        String account = body.getOrDefault("account", "");
        if (account.isEmpty()) return ApiResponse.error("account required");
        boolean ok = userService.markVerifiedByAccount(account);
        if (!ok) return ApiResponse.error("account not found");
        return ApiResponse.ok(Map.of("message", "verified"));
    }

    private String csv(String s) {
        if (s == null) return "";
        String escaped = s.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private void refreshQuestionCount(Long scaleId) {
        if (scaleId == null) return;
        long count = questionMapper.selectCount(new QueryWrapper<Question>().eq("scale_id", scaleId));
        Scale s = scaleMapper.selectById(scaleId);
        if (s == null) return;
        s.setQuestionCount((int) count);
        scaleMapper.updateById(s);
    }
}
