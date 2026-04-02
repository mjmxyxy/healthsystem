<template>
  <div class="chat-page">
    <el-card>
      <div class="chat-head">
        <div class="left">
          <el-button text @click="closeChat">← 返回</el-button>
          <div class="avatar">咨</div>
          <div>
            <div class="name">咨询师 {{ counselorId }}</div>
            <div class="sub">{{ statusText }}</div>
          </div>
        </div>
        <el-button text>⋯</el-button>
      </div>

      <div class="chat-window" ref="win" @scroll="onScroll">
        <div class="history-loading" v-if="loadingHistory">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载历史中...</span>
        </div>

        <div v-for="m in displayMessages" :key="m.id || m.timeKey" class="msg-wrap">
          <div v-if="m.type === 'time'" class="time-sep">{{ m.label }}</div>
          <div
            v-else
            :class="['msg', m.from === userId ? 'user' : 'other']"
            @contextmenu.prevent="openMenu($event, m)"
            @mousedown="startPress($event, m)"
            @mouseup="endPress"
            @mouseleave="endPress"
          >
            <div class="avatar-sm">{{ m.from === userId ? '我' : '咨' }}</div>
            <div class="bubble">{{ m.text }}</div>
          </div>
        </div>

        <div class="typing" v-if="peerTyping">对方正在输入...</div>
      </div>

      <div class="quick-reactions">
        <button class="quick-btn" v-for="q in quickReactions" :key="q" @click="sendQuick(q)">{{ q }}</button>
      </div>

      <div class="composer">
        <button class="icon-btn" @click="noop">🎤</button>
        <input
          class="text-input"
          v-model="text"
          placeholder="输入你想说的话..."
          @keydown.enter.prevent="onEnter"
          @input="sendTyping"
        />
        <button class="icon-btn" @click="noop">😊</button>
        <button class="send-btn" :disabled="sending" @click="send">➤</button>
      </div>

      <div class="tips">按 Enter 发送，Shift+Enter 换行</div>

      <el-popover
        v-model:visible="menuVisible"
        trigger="manual"
        :virtual-ref="menuVirtualRef"
        virtual-triggering
        placement="top"
        width="120"
      >
        <div class="menu-list">
          <el-button text @click="copyMsg">复制</el-button>
          <el-button text @click="deleteMsg">删除</el-button>
          <el-button text type="danger" @click="reportMsg">举报</el-button>
        </div>
      </el-popover>
    </el-card>
  </div>
</template>

<script setup>
import { Loading } from '@element-plus/icons-vue'
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const counselorId = Number(route.params.id || route.params.counselorId)
const messages = ref([])
const text = ref('')
const sending = ref(false)
const ws = ref(null)
const win = ref(null)
const userId = Number(localStorage.getItem('user_id') || 0)
const status = ref('disconnected')
const loadingHistory = ref(false)
const peerTyping = ref(false)
const quickReactions = ['今天有点焦虑😰', '感觉好多了😊', '需要建议💡']
const typingTimer = ref(null)
const pressTimer = ref(null)
const selectedMsg = ref(null)
const menuVisible = ref(false)
const menuVirtualRef = ref({ getBoundingClientRect: () => DOMRect.fromRect({ x: 0, y: 0, width: 0, height: 0 }) })

const statusText = computed(() => {
  if (status.value === 'connected') return '已连接'
  if (status.value === 'connecting') return '连接中...'
  return '未连接'
})

const displayMessages = computed(() => {
  const out = []
  let last = null
  for (const m of messages.value) {
    const t = new Date(m.time || Date.now())
    if (!last || Math.abs(t.getTime() - last.getTime()) > 5 * 60 * 1000) {
      out.push({ type: 'time', timeKey: `t-${t.getTime()}`, label: t.toLocaleString('zh-CN', { hour12: false }) })
      last = t
    }
    out.push(m)
  }
  return out
})

const scrollBottom = () => {
  nextTick(() => {
    const el = win.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

const connect = () => {
  if (!userId) { window.$message?.error('请先登录'); return }
  if (ws.value && ws.value.readyState === WebSocket.OPEN) { window.$message?.info('已连接'); return }
  status.value = 'connecting'
  const scheme = location.protocol === 'https:' ? 'wss' : 'ws'
  const wsPort = '8082'
  const url = `${scheme}://localhost:${wsPort}/ws/chat?userId=${userId}`
  ws.value = new WebSocket(url)
  ws.value.onopen = () => { status.value = 'connected'; window.$message?.success('WebSocket 已连接') }
  ws.value.onclose = () => { status.value = 'disconnected'; window.$message?.warning('WebSocket 已断开') }
  ws.value.onerror = (e) => { console.error('ws error', e); window.$message?.error('WebSocket 错误') }
  ws.value.onmessage = (ev) => {
    try {
      const obj = JSON.parse(ev.data)
      if (obj.type === 'typing') {
        peerTyping.value = true
        setTimeout(() => { peerTyping.value = false }, 1500)
        return
      }
      if (obj.error) {
        window.$message?.warning(obj.error)
        return
      }
      messages.value.push({
        id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
        from: obj.fromUserId ? Number(obj.fromUserId) : 0,
        text: obj.text || ev.data,
        time: new Date().toISOString()
      })
    } catch (ex) {
      messages.value.push({ id: `${Date.now()}-raw`, from: 0, text: ev.data, time: new Date().toISOString() })
    }
    scrollBottom()
  }
}

const sendTyping = () => {
  if (!ws.value || ws.value.readyState !== WebSocket.OPEN) return
  if (typingTimer.value) return
  typingTimer.value = setTimeout(() => {
    typingTimer.value = null
  }, 1200)
  ws.value.send(JSON.stringify({ type: 'typing', toUserId: String(counselorId), fromUserId: String(userId) }))
}

const send = () => {
  if (!text.value.trim()) return
  if (!ws.value || ws.value.readyState !== WebSocket.OPEN) { window.$message?.error('尚未连接 WebSocket，请先点击连接'); return }
  sending.value = true
  const payload = { toUserId: String(counselorId), fromUserId: String(userId), text: text.value.trim() }
  ws.value.send(JSON.stringify(payload))
  messages.value.push({
    id: `${Date.now()}-self`,
    from: userId,
    text: text.value.trim(),
    time: new Date().toISOString()
  })
  text.value = ''
  scrollBottom()
  sending.value = false
}

const onEnter = (e) => {
  if (e.shiftKey) return
  send()
}

const closeChat = () => {
  router.push('/student/appointments')
}

const sendQuick = (q) => {
  text.value = q
  send()
}

const noop = () => window.$message?.info('该功能开发中')

const onScroll = () => {
  const el = win.value
  if (!el || el.scrollTop > 0 || loadingHistory.value) return
  loadingHistory.value = true
  setTimeout(() => {
    messages.value = [
      {
        id: `${Date.now()}-history`,
        from: counselorId,
        text: '这里是更早的聊天记录（示例）。',
        time: new Date(Date.now() - 3600 * 1000).toISOString()
      },
      ...messages.value
    ]
    loadingHistory.value = false
  }, 700)
}

const startPress = (evt, msg) => {
  endPress()
  pressTimer.value = setTimeout(() => {
    openMenu(evt, msg)
  }, 450)
}

const endPress = () => {
  if (pressTimer.value) {
    clearTimeout(pressTimer.value)
    pressTimer.value = null
  }
}

const openMenu = (evt, msg) => {
  selectedMsg.value = msg
  menuVirtualRef.value = {
    getBoundingClientRect: () => DOMRect.fromRect({ x: evt.clientX, y: evt.clientY, width: 0, height: 0 })
  }
  menuVisible.value = true
}

const copyMsg = async () => {
  if (!selectedMsg.value) return
  try {
    await navigator.clipboard.writeText(selectedMsg.value.text || '')
    window.$message?.success('已复制')
  } catch (_) {
    window.$message?.error('复制失败')
  }
  menuVisible.value = false
}

const deleteMsg = () => {
  if (!selectedMsg.value) return
  messages.value = messages.value.filter((m) => m.id !== selectedMsg.value.id)
  menuVisible.value = false
}

const reportMsg = () => {
  window.$message?.success('已提交举报')
  menuVisible.value = false
}

onMounted(() => {
  connect()
})

onBeforeUnmount(() => {
  endPress()
  if (ws.value) ws.value.close()
})
</script>

<style scoped>
.chat-page { max-width:1080px; margin:0 auto }
.chat-head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 10px; }
.left { display:flex; align-items:center; gap:10px; }
.avatar {
  width:40px;
  height:40px;
  border-radius:50%;
  background: linear-gradient(135deg, var(--color-student), var(--color-primary));
  color:#fff;
  display:flex;
  align-items:center;
  justify-content:center;
  font-weight:700;
}
.name { color: var(--color-text-primary); font-weight: var(--font-weight-semibold); }
.sub { color: var(--color-text-secondary); font-size: var(--font-size-xs); }

.chat-window {
  height: 420px;
  overflow: auto;
  padding: 14px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-secondary);
  border-radius: 12px;
}

.history-loading {
  display:flex;
  align-items:center;
  justify-content:center;
  gap:6px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  margin-bottom: 10px;
}

.msg-wrap { margin: 8px 0; }
.time-sep {
  text-align:center;
  font-size: 12px;
  color: var(--color-text-tertiary);
  margin: 16px 0;
}

.msg {
  display:flex;
  align-items:flex-end;
  gap:8px;
}

.msg.user {
  justify-content:flex-end;
}

.msg.user .avatar-sm {
  order: 2;
}

.msg.user .bubble {
  order: 1;
  background: var(--color-student);
  color: #fff;
  border-radius: 18px 4px 18px 18px;
}

.msg.other {
  justify-content:flex-start;
}

.msg.other .bubble {
  background: #fff;
  color: var(--color-text-primary);
  border-radius: 4px 18px 18px 18px;
}

.avatar-sm {
  width:40px;
  height:40px;
  border-radius:50%;
  background: #dbeafe;
  color: #1e3a8a;
  display:flex;
  align-items:center;
  justify-content:center;
  font-size: 13px;
}

.bubble {
  max-width: 70%;
  padding: 10px 12px;
  box-shadow: var(--shadow-sm);
  line-height: 1.55;
}

.typing {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  margin-top: 10px;
}

.quick-reactions {
  margin-top: 12px;
  display:flex;
  gap:8px;
  overflow-x:auto;
}

.quick-btn {
  white-space: nowrap;
  border:1px solid var(--color-border);
  border-radius: 999px;
  height: 32px;
  padding: 0 12px;
  background:#fff;
  cursor:pointer;
}

.composer {
  margin-top:12px;
  display:flex;
  align-items:center;
  gap:8px;
}

.icon-btn {
  border: 1px solid var(--color-border);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background:#fff;
  cursor:pointer;
}

.text-input {
  flex:1;
  height:44px;
  border-radius:24px;
  border:1px solid var(--color-border);
  background:#F3F4F6;
  padding:0 14px;
  outline:none;
}

.send-btn {
  width:40px;
  height:40px;
  border:none;
  border-radius:50%;
  background: var(--color-student);
  color:#fff;
  cursor:pointer;
}

.tips {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-xs);
  margin-top: 6px;
}

.menu-list {
  display:flex;
  flex-direction:column;
  align-items:flex-start;
}
</style>
