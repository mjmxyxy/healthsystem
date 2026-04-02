<template>
  <div class="ai-chat-page">
    <el-card>
      <h3>AI 心理助手</h3>
      <div class="chat-window" ref="win">
        <div v-for="(m, idx) in messages" :key="idx" :class="['msg', m.from]">
          <div class="bubble">{{ m.text }}</div>
        </div>
      </div>

      <div class="composer">
        <el-input type="textarea" v-model="text" :rows="2" @keydown.enter.prevent="onEnterSend"></el-input>
        <div style="margin-top:8px; display:flex; justify-content:flex-end">
          <el-button :loading="loading" type="primary" @click="send">发送</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import axios from 'axios'

const messages = ref([])
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
    push('bot', '请求出错，请检查后端或网络')
  } finally {
    loading.value = false
  }
}

const onEnterSend = (e) => {
  // shift+enter 插入换行
  if (e.shiftKey) {
    return
  }
  send()
}
</script>

<style scoped>
.ai-chat-page { max-width:900px; margin:24px auto }
.chat-window { height:400px; overflow:auto; padding:12px; border:1px solid #eee; background:#fafafa }
.msg { margin:8px 0; display:flex }
.msg.user { justify-content:flex-end }
.msg.bot { justify-content:flex-start }
.bubble { max-width:70%; padding:10px 12px; border-radius:8px; background:#fff }
.msg.user .bubble { background:#409EFF; color:#fff }
</style>
