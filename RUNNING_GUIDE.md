# Running Guide

## 1. 当前是否可以直接运行

可以。当前代码已经通过：

- 前端构建通过（Vite build）
- 后端编译通过（Maven compile）

你终端里出现过的 `mvn spring-boot:run` Exit Code 1，通常是以下两种情况之一：

- 启动后被手动中断（Ctrl+C）
- 启动过程中出现一次 DevTools 重启异常后又恢复

从日志看，后端在后续已成功启动到 8082。

## 2. 环境要求

- Java 17
- Maven 3.6+
- Node.js 18+
- MySQL 8

## 3. 一次性初始化数据库

### 方式 A：使用脚本（推荐）

在项目根目录执行：

```powershell
./scripts/setup-db.ps1
```

### 方式 B：手工导入

导入以下 SQL 到 MySQL：

- sql/schema.sql

默认数据库名：healthy_db

## 4. 启动后端

### 方式 A：使用脚本（推荐）

```powershell
./scripts/start-backend.ps1
```

默认会用以下参数：

- 数据库 URL: jdbc:mysql://127.0.0.1:3306/healthy_db?useSSL=false&serverTimezone=UTC
- 用户名: healthy
- 密码: healthypass

如果你的库不是这个账号密码，可显式传参：

```powershell
./scripts/start-backend.ps1 -DbUser root -DbPassword root
```

### 方式 B：手工启动

```powershell
cd backend
mvn spring-boot:run
```

启动成功标志：日志出现 `Tomcat started on port(s): 8082`。

## 5. 启动前端

### 方式 A：使用脚本（推荐）

```powershell
./scripts/start-frontend.ps1
```

### 方式 B：手工启动

```powershell
cd frontend
npm install
npm run dev
```

默认访问地址：

- 前端: http://localhost:5173
- 后端: http://localhost:8082

## 6. 管理员端登录与访问

管理员前端路由：

- /admin/dashboard
- /admin/students
- /admin/counselors
- /admin/scales
- /admin/appointments
- /admin/articles
- /admin/profile

管理员接口前缀：

- /api/admin

### 6.1 现成演示账号

系统默认仅自动创建：

- student_demo / 123456
- counselor_demo / 123456
- admin_demo / 123456

你可以直接用 `admin_demo` 登录管理员端。

### 6.2 创建管理员账号（开发环境）

当前注册接口支持传 role 字段，你可以先注册一个 admin 再登录。

示例请求：

```http
POST /api/auth/register
Content-Type: application/json

{
  "account": "admin_demo",
  "password": "123456",
  "name": "管理员演示",
  "role": "admin"
}
```

然后在登录页选择“管理员”身份登录。

## 7. 管理员端功能清单

- 控制台：统计学生、咨询师、测评、预约、待审核文章
- 学生管理：列表、详情、禁用/恢复、导出 CSV
- 咨询师管理：审核、上下架、编辑
- 量表题目管理：量表 CRUD、题目 CRUD
- 预约订单管理：全平台预约查看
- 文章审核：通过/驳回
- 个人中心：资料修改、密码修改

## 8. 联调检查（建议按顺序）

1. 访问登录页并登录 admin。
2. 打开 /admin/dashboard，确认统计有返回。
3. 打开 /admin/students，执行一次禁用/恢复。
4. 打开 /admin/counselors，执行审核/上下架。
5. 打开 /admin/scales，新增量表与题目。
6. 打开 /admin/articles，审核一篇文章。
7. 打开 /admin/profile，修改资料并改密。

## 9. 常见问题排查

### 9.1 后端端口占用

```powershell
Get-NetTCPConnection -LocalPort 8082 -State Listen
```

若被占用，结束对应进程后再启动。

### 9.2 数据库连接失败

优先检查：

- MySQL 是否启动
- healthy_db 是否存在
- application.yml 或脚本参数中的账号密码是否正确

### 9.3 登录报 SQL 字段缺失（历史库兼容）

如果你使用的是旧库结构，建议：

- 重新执行 schema.sql
- 或让项目启动一次，兼容初始化器会尝试补齐关键结构

### 9.4 前端有页面但接口 401/403

检查登录后的 localStorage：

- token 是否存在
- role 是否为 admin

## 10. 可选：AI 参数（若你要测试 AI 对话）

后端支持通过环境变量配置：

- AI_API_KEY 或 ARK_API_KEY
- AI_API_URL
- AI_MODEL

示例（方舟）：

```powershell
$env:ARK_API_KEY="<your_key>"
$env:AI_API_URL="https://ark.cn-beijing.volces.com/api/v3"
$env:AI_MODEL="<your_endpoint_or_model>"
```

再执行后端启动命令即可。
