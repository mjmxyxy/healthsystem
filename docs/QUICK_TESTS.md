快速联调脚本（curl / wscat）

准备：
- 后端默认地址： http://localhost:8080
- 前端默认 dev： http://localhost:5173
- 推荐工具：`jq`（JSON 解析）、`wscat`（WebSocket 客户端，npm install -g wscat）

1) 注册（示例学生账号）

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"account":"student1","password":"pass","name":"学生1","studentId":"2026001","gender":"M","role":"student"}' | jq
```

2) 登录并保存 token/user_id

```bash
RESP=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"student1","password":"pass","role":"student"}')
TOKEN=$(echo "$RESP" | jq -r '.data.token')
USER_ID=$(echo "$RESP" | jq -r '.data.userId')
echo "TOKEN=$TOKEN USER_ID=$USER_ID"
```

3) 测试 AI 心理助手（需要已在后端配置 `OPENAI_API_KEY`）

```bash
curl -s -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"message":"我最近很焦虑，怎么办？"}' | jq '.data.reply'
```

4) 列出咨询师

```bash
curl -s http://localhost:8080/api/counselors | jq '.data'
```

5) 创建预约（演示用，内存存储）

```bash
curl -s -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"studentId':$USER_ID, "counselorId":1001, "startTime":"2026-04-02T10:00", "endTime":"2026-04-02T10:30"}' | jq
```

6) 查看我的预约

```bash
curl -s "http://localhost:8080/api/appointments?studentId=$USER_ID" | jq '.data'
```

7) 取消预约

```bash
curl -s -X DELETE "http://localhost:8080/api/appointments/1001?studentId=$USER_ID" \
  -H "Authorization: Bearer $TOKEN" | jq
```

8) WebSocket 即时聊天测试（使用 `wscat`）

- 启动客户端（以 userId=1 为例）：

```bash
wscat -c "ws://localhost:8080/ws/chat?userId=1"
```

- 在 wscat 中发送给目标用户（例如 1002）:

```json
{"toUserId":"1002","fromUserId":"1","text":"你好，我是学生，测试实时通信"}
```

说明：
- 预约模块为演示版，使用内存存储（服务重启会丢失）。
- AI 功能需要配置 `OPENAI_API_KEY`（或兼容 API）；也可在 `OPENAI_API_URL` 中替换为企业/自托管模型地址。
- 若需 Postman 集合，我可以生成并导出为 JSON。