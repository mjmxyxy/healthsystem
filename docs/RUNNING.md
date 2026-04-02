快速运行与联调说明

环境要求：
- JDK 17+
- Maven
- Node.js 16+

后端启动：
1. 进入 `backend` 目录
2. 设置 OpenAI API Key（可选，AI 功能需要）：

PowerShell 示例：
```powershell
$env:OPENAI_API_KEY = 'your_api_key_here'
```

3. 启动：
```powershell
mvn spring-boot:run
```

前端启动：
1. 进入 `frontend` 目录
2. 安装并启动：
```bash
npm install
npm run dev
```

关键接口：
- `POST /api/auth/login` 登录（返回统一结构 `code/message/data`）
- `POST /api/ai/chat` AI 聊天代理，请求体 `{message: string}`，返回 `{code:0,message:'ok',data:{reply: '...'}}`
- `POST /api/appointments` 创建预约（演示使用内存存储，不持久化）
- WebSocket: `ws://<host>:<port>/ws/chat?userId=<yourId>`，消息格式 JSON `{toUserId:'<id>',fromUserId:'<id>',text:'...'} `，服务器会把消息转发到目标 `userId` 的连接

注意：
- 演示版使用内存存储预约，服务重启会丢失数据。
- 若使用 OpenAI，请确保 `OPENAI_API_KEY` 与 `OPENAI_API_URL`（可选）已配置。

简单联调步骤：
1. 启动后端、前端
2. 在前端注册 / 登录为学生，记下登录时返回的 `user_id`
3. 访问 `学生 -> AI 心理助手` 测试对话
4. 在 `咨询师列表` 创建预约并在 `我的预约` 查看
5. 在 `我的预约` 点击进入聊天，前端将使用 WebSocket 与后端转发消息（无需数据库）

如需我生成演示用的 API 调用脚本或录屏脚本，我可以继续补充。