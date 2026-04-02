<template>
  <div class="page counselor-page">
    <el-card>
      <div class="head">
        <div>
          <h2>实时聊天（学生ID: {{ studentId }}）</h2>
          <div class="status">{{ statusText }}</div>
        </div>
        <el-button type="warning" plain @click="openReferral">转介</el-button>
      </div>

      <div class="chat-window" ref="win">
        <div v-for="(m, i) in messages" :key="i" :class="['msg', m.from === counselorId ? 'user' : 'other']">
          <div class="bubble">{{ m.text }}</div>
        </div>
      </div>

      <div class="quick-row">
        <button class="quick-btn" v-for="t in templates" :key="t" @click="insertTemplate(t)">{{ t }}</button>
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

    <el-dialog v-model="showReferral" title="转介学生" width="520px">
      <el-select v-model="targetCounselor" placeholder="选择目标咨询师" style="width:100%">
        <el-option label="咨询师A" value="A" />
        <el-option label="咨询师B" value="B" />
        <el-option label="咨询师C" value="C" />
      </el-select>
      <el-input v-model="referralReason" type="textarea" :rows="4" style="margin-top:10px" placeholder="填写转介原因" />
      <template #footer>
        <el-button @click="showReferral = false">取消</el-button>
        <el-button type="primary" @click="confirmReferral">确认转介</el-button>
      </template>
    </el-dialog>
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
const templates = ['请做一次深呼吸，我们慢慢来。', '你愿意说说刚刚发生了什么吗？', '我们先一起梳理最困扰你的点。']
const showReferral = ref(false)
const targetCounselor = ref('')
const referralReason = ref('')

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

const insertTemplate = (t) => {
  text.value = t
}

const openReferral = () => {
  showReferral.value = true
}

const confirmReferral = () => {
  if (!targetCounselor.value) {
    window.$message?.warning('请选择目标咨询师')
    return
  }
  window.$message?.success(`已转介给咨询师${targetCounselor.value}`)
  showReferral.value = false
  targetCounselor.value = ''
  referralReason.value = ''
}

onMounted(connect)
onBeforeUnmount(() => {
  if (ws.value) ws.value.close()
})
</script>

<style scoped>
.page { max-width:1080px; margin:0 auto }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 10px }
.status { color: var(--color-text-secondary); font-size: 13px; margin-top: 2px; }
.chat-window { height:460px; overflow:auto; padding:14px; border:1px solid #e5e7eb; background:#f9fafb; border-radius:12px }
.msg { margin:8px 0; display:flex }
.msg.user { justify-content:flex-end }
.msg.other { justify-content:flex-start }
.bubble { max-width:70%; padding:10px 12px; border-radius:12px; background:#fff; border:1px solid #e5e7eb }
.msg.user .bubble { background:var(--color-counselor); color:#fff; border-color:var(--color-counselor) }
.quick-row { margin-top: 10px; display:flex; gap:8px; overflow:auto; }
.quick-btn {
  height: 32px;
  border-radius: 999px;
  border: 1px solid #ddd6fe;
  background: #faf5ff;
  color: #6d28d9;
  white-space: nowrap;
  padding: 0 12px;
  cursor: pointer;
}
.composer { margin-top: 12px }
.toolbar { margin-top:8px; display:flex; justify-content:space-between; align-items:center }
.hint { color:#6b7280 }
</style>
