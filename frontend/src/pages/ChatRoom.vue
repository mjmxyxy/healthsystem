<template>
  <div class="chat-page">
    <el-card>
      <div style="display:flex; justify-content:space-between; align-items:center">
        <h3>与咨询师聊天（ID: {{ counselorId }})</h3>
        <div style="display:flex; align-items:center; gap: 10px">
          <div>{{ statusText }}</div>
          <el-button size="small" @click="closeChat">关闭</el-button>
        </div>
      </div>

      <div class="chat-window" ref="win">
        <div v-for="(m, i) in messages" :key="i" :class="['msg', m.from === userId ? 'user' : 'other']">
          <div class="bubble">{{ m.text }}</div>
        </div>
      </div>

      <div class="composer">
        <el-input type="textarea" v-model="text" :rows="2" @keydown.enter.prevent="onEnter"></el-input>
        <div style="margin-top:8px; display:flex; justify-content:space-between; align-items:center">
          <div style="color:#888">按 Enter 发送，Shift+Enter 换行</div>
          <div>
            <el-button @click="connect" size="small">连接</el-button>
            <el-button type="primary" :loading="sending" @click="send">发送</el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
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

const statusText = computed(() => {
  if (status.value === 'connected') return '已连接'
  if (status.value === 'connecting') return '连接中...'
  return '未连接'
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
      // push message
      messages.value.push({ from: obj.fromUserId ? Number(obj.fromUserId) : 0, text: obj.text || ev.data })
    } catch (ex) {
      messages.value.push({ from: 0, text: ev.data })
    }
    scrollBottom()
  }
}

const send = () => {
  if (!text.value.trim()) return
  if (!ws.value || ws.value.readyState !== WebSocket.OPEN) { window.$message?.error('尚未连接 WebSocket，请先点击连接'); return }
  sending.value = true
  const payload = { toUserId: String(counselorId), fromUserId: String(userId), text: text.value.trim() }
  ws.value.send(JSON.stringify(payload))
  messages.value.push({ from: userId, text: text.value.trim() })
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

onMounted(() => {
  // auto connect
  connect()
})

onBeforeUnmount(() => {
  if (ws.value) ws.value.close()
})
</script>

<style scoped>
.chat-page { max-width:900px; margin:24px auto }
.chat-window { height:420px; overflow:auto; padding:14px; border:1px solid #e5e7eb; background:#f9fafb; border-radius: 12px }
.msg { margin:8px 0; display:flex }
.msg.user { justify-content:flex-end }
.msg.other { justify-content:flex-start }
.bubble { max-width:70%; padding:10px 12px; border-radius:12px; background:#fff; border:1px solid #e5e7eb }
.msg.user .bubble { background:#3B82F6; color:#fff; border-color:#3B82F6 }
.composer { margin-top:12px }
</style>
