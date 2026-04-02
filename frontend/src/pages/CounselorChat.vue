<template>
  <div class="page">
    <el-card>
      <div class="head">
        <h2>实时聊天（学生ID: {{ studentId }}）</h2>
        <div>{{ statusText }}</div>
      </div>

      <div class="chat-window" ref="win">
        <div v-for="(m, i) in messages" :key="i" :class="['msg', m.from === counselorId ? 'user' : 'other']">
          <div class="bubble">{{ m.text }}</div>
        </div>
      </div>

      <div class="composer">
        <el-input type="textarea" v-model="text" :rows="2" @keydown.enter.prevent="onEnter"></el-input>
        <div class="toolbar">
          <div class="hint">Enter 发送，Shift+Enter 换行</div>
          <div>
            <el-button size="small" @click="connect">连接</el-button>
            <el-button type="primary" :loading="sending" @click="send">发送</el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const studentId = Number(route.params.studentId)
const counselorId = Number(localStorage.getItem('user_id') || 0)

const messages = ref([])
const text = ref('')
const sending = ref(false)
const ws = ref(null)
const status = ref('disconnected')
const win = ref(null)

const statusText = computed(() => {
  if (status.value === 'connected') return '已连接'
  if (status.value === 'connecting') return '连接中...'
  return '未连接'
})

const scrollBottom = () => {
  nextTick(() => {
    if (win.value) win.value.scrollTop = win.value.scrollHeight
  })
}

const connect = () => {
  if (!counselorId) {
    window.$message?.error('未获取到咨询师ID')
    return
  }
  if (ws.value && ws.value.readyState === WebSocket.OPEN) {
    window.$message?.info('已连接')
    return
  }
  status.value = 'connecting'
  const scheme = location.protocol === 'https:' ? 'wss' : 'ws'
  const url = `${scheme}://localhost:8082/ws/chat?userId=${counselorId}`
  ws.value = new WebSocket(url)
  ws.value.onopen = () => {
    status.value = 'connected'
    window.$message?.success('连接成功')
  }
  ws.value.onclose = () => {
    status.value = 'disconnected'
  }
  ws.value.onerror = () => {
    window.$message?.error('WebSocket 连接失败')
  }
  ws.value.onmessage = (ev) => {
    try {
      const obj = JSON.parse(ev.data)
      if (obj.error) {
        window.$message?.warning(obj.error)
        return
      }
      messages.value.push({ from: Number(obj.fromUserId || 0), text: obj.text || ev.data })
    } catch (_) {
      messages.value.push({ from: 0, text: ev.data })
    }
    scrollBottom()
  }
}

const send = () => {
  if (!text.value.trim()) return
  if (!ws.value || ws.value.readyState !== WebSocket.OPEN) {
    window.$message?.error('尚未连接')
    return
  }
  sending.value = true
  const payload = {
    fromUserId: String(counselorId),
    toUserId: String(studentId),
    text: text.value.trim()
  }
  ws.value.send(JSON.stringify(payload))
  messages.value.push({ from: counselorId, text: text.value.trim() })
  text.value = ''
  scrollBottom()
  sending.value = false
}

const onEnter = (e) => {
  if (e.shiftKey) return
  send()
}

onMounted(connect)
onBeforeUnmount(() => {
  if (ws.value) ws.value.close()
})
</script>

<style scoped>
.page { max-width:1080px; margin:0 auto }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 10px }
.chat-window { height:460px; overflow:auto; padding:14px; border:1px solid #e5e7eb; background:#f9fafb; border-radius:12px }
.msg { margin:8px 0; display:flex }
.msg.user { justify-content:flex-end }
.msg.other { justify-content:flex-start }
.bubble { max-width:70%; padding:10px 12px; border-radius:12px; background:#fff; border:1px solid #e5e7eb }
.msg.user .bubble { background:#3B82F6; color:#fff; border-color:#3B82F6 }
.composer { margin-top: 12px }
.toolbar { margin-top:8px; display:flex; justify-content:space-between; align-items:center }
.hint { color:#6b7280 }
</style>
