<template>
  <div class="ai-chat-page">
    <div class="chat-window" ref="win">
      <div v-for="(m, idx) in messages" :key="idx" :class="['msg', m.from]">
        <div class="bubble">{{ m.text }}</div>
      </div>
    </div>

    <div class="composer">
      <el-input type="textarea" v-model="text" :rows="3" @keydown.enter.prevent="onEnterSend"></el-input>
      <div style="margin-top:8px; display:flex; justify-content:flex-end">
        <el-button :loading="loading" type="primary" @click="send">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import axios from 'axios'

const messages = ref([{ from: 'bot', text: '你好，我在这里陪你。你可以直接告诉我最近的感受。' }])
const text = ref('')
const loading = ref(false)
const win = ref(null)

const push = (from, t) => {
  messages.value.push({ from, text: t })
  nextTick(() => {
    const el = win.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

const send = async () => {
  if (!text.value.trim()) return
  const t = text.value.trim()
  push('user', t)
  text.value = ''
  loading.value = true
  try {
    const res = await axios.post('/api/ai/chat', { message: t })
    const body = res.data || {}
    const data = body.data || body
    const reply = data.reply || '(无回复)'
    push('bot', reply)
  } catch (err) {
    console.error(err)
    const detail = err?.response?.data?.detail || err?.response?.data?.error || err?.message
    push('bot', `请求出错：${detail || '请检查后端或网络'}`)
  } finally {
    loading.value = false
  }
}

const onEnterSend = (e) => {
  if (e.shiftKey) return
  send()
}

defineExpose({ push })
</script>

<style scoped>
.ai-chat-page { display:flex; flex-direction:column; gap:12px }
.chat-window { height:360px; overflow:auto; padding:12px; border:1px solid #e5e7eb; background:#fafafa; border-radius:12px }
.msg { margin:8px 0; display:flex }
.msg.user { justify-content:flex-end }
.msg.bot { justify-content:flex-start }
.bubble { max-width:78%; padding:10px 12px; border-radius:14px; background:#fff; line-height:1.6 }
.msg.user .bubble { background:#409EFF; color:#fff }
.composer { margin-top:4px }
</style>
