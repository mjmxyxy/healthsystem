项目：校园心理咨询平台（轻量化前后端分离骨架）

快速运行与管理员端说明请查看：`RUNNING_GUIDE.md`

本仓库为按需求生成的初始脚手架，包含：

- 后端（Spring Boot + MyBatis-Plus + MySQL + WebSocket）骨架模板
- 前端（Vue3 + Vite + Element Plus + Pinia）骨架模板
- Docker Compose 用于校园单机轻量部署（MySQL、后端、前端）
- 数据库建表 SQL（基础实体）
- OpenAPI（基础路径示例）
- Sprint 任务分解（2-4 周）

注意：这是初始骨架，含示例配置与核心模块结构。后续需要：完善业务逻辑、添加测试、补充安全策略、对接学校学籍数据/短信/文件存储服务、部署与运维。

下一步建议：
1. 在开发环境导入 SQL（e.g. MySQL 8）并更新 application.yml 数据库凭据。
2. 在 IDE 中打开 backend 子项目（Maven），运行 Spring Boot 应用。
3. 在 frontend 目录安装依赖并启动开发服务器（Vite）。

生成清单（部分重要文件）：
- docker-compose.yml
- backend/
  - pom.xml
  - src/main/java/com/healthy/SystemApplication.java
  - src/main/java/com/healthy/controller/AuthController.java
  - src/main/java/com/healthy/util/AESUtil.java
  - src/main/resources/application.yml
- frontend/
  - package.json
  - src/main.ts
  - src/pages/Login.vue
- sql/schema.sql
- openapi.yaml
- sprint.md

如需我继续：我可以把后端完整模块（auth、user、counselor、appointment、assessment、chat、admin）的初始代码补全，或生成详细的 OpenAPI 文档与前端页面。请指示具体优先级。
