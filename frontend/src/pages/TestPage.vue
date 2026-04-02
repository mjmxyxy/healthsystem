<template>
  <div class="page">
    <el-card v-loading="loading">
      <h2>在线答题 - {{ scaleName }}</h2>
      <p class="hint" style="margin-top:-4px">共 {{ questions.length }} 题，已作答 {{ answeredCount }} 题。</p>

      <div class="progress-bar" v-if="questions.length > 0">
        <div class="progress-fill" :style="{ width: ((answeredCount / questions.length) * 100).toFixed(0) + '%' }"></div>
      </div>

      <el-empty v-if="!loading && questions.length === 0" description="暂无题目" />

      <div v-else class="question card-hover">
        <div class="progress">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</div>
        <p><strong>{{ currentQuestion.seq || currentIndex + 1 }}.</strong> {{ currentQuestion.text }}</p>
        <el-radio-group v-model="answers[currentQuestion.id]">
          <el-radio v-for="opt in parseOptions(currentQuestion.options)" :key="opt.value" :label="opt.value">
            {{ opt.label }}
          </el-radio>
        </el-radio-group>
      </div>

      <div style="margin-top:16px; display:flex; justify-content:space-between; align-items:center">
        <div>
          <el-button @click="backToScales">返回量表列表</el-button>
        </div>
        <div>
          <el-button :disabled="currentIndex === 0" @click="prevQuestion">上一题</el-button>
          <el-button :disabled="currentIndex >= questions.length - 1" @click="nextQuestion">下一题</el-button>
          <el-button type="primary" :disabled="!allAnswered || submitting" :loading="submitting" @click="submit">提交测评</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)
const questions = ref([])
const answers = ref({})
const scaleName = ref(route.query.name || `量表 ${id}`)
const currentIndex = ref(0)
const loading = ref(false)
const submitting = ref(false)

const currentQuestion = computed(() => questions.value[currentIndex.value] || {})
const answeredCount = computed(() => questions.value.filter(q => answers.value[q.id] !== undefined && answers.value[q.id] !== null).length)
const allAnswered = computed(() => questions.value.length > 0 && answeredCount.value === questions.value.length)

const parseOptions = (raw) => {
  try {
    const arr = JSON.parse(raw || '[]')
    if (!Array.isArray(arr) || arr.length === 0) throw new Error('empty')
    return arr.map((item, index) => {
      const text = String(item)
      const m = text.match(/^(\d+)\s*(.*)$/)
      if (m) {
        return { value: Number(m[1]), label: text }
      }
      return { value: index, label: text }
    })
  } catch (_) {
    return [0, 1, 2, 3, 4].map(v => ({ value: v, label: String(v) }))
  }
}

const load = async () => {
  if (!id || Number.isNaN(id)) return
  loading.value = true
  try {
    const r = await axios.get(`/api/scale/${id}/questions?page=1&pageSize=500`)
    const body = r.data || {}
    const list = body.data || body
    questions.value = Array.isArray(list) ? list : []
    if (questions.value.length > 0 && !scaleName.value) {
      scaleName.value = `量表 ${id}`
    }
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (!allAnswered.value) {
    window.$message?.error('还有未作答题目，暂不能提交')
    return
  }

  try {
    await ElMessageBox.confirm('确认提交测评吗？提交后将生成报告。', '提交确认', { type: 'warning' })
  } catch (_) {
    return
  }

  // collect answers for all questions
  const payload = { answers: [] }
  for (const q of questions.value) {
    const a = answers.value[q.id]
    if (a === undefined || a === null) {
      // require all visible answered
      await window.$message?.error('请完成本页全部题目')
      return
    }
    payload.answers.push({ questionId: q.id, answer: a })
  }

  const userId = Number(localStorage.getItem('user_id') || 0)
  if (userId > 0) payload.userId = userId

  submitting.value = true
  try {
    const res = await axios.post(`/api/scale/${id}/submit`, payload)
    const body = res.data || {}
    const data = body.data || body
    const reportId = data.id || data.reportId
    window.$message?.success('测评提交成功')
    if (reportId) {
      router.push(`/student/assessment/${reportId}`)
      return
    }
    router.push('/student/assessments')
  } catch (err) {
    const resp = err?.response?.data || {}
    const msg = resp?.msg || resp?.message || resp?.error || '提交失败'
    window.$message?.error(msg)
  } finally {
    submitting.value = false
  }
}

const prevQuestion = () => {
  if (currentIndex.value > 0) currentIndex.value -= 1
}

const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) currentIndex.value += 1
}

const backToScales = () => router.push('/student/scales')

onMounted(load)
</script>

<style scoped>
.page { max-width:960px; margin: 24px auto; padding: 0 16px }
.question { margin: 12px 0; padding: 16px; border: 1px solid #e5e7eb; border-radius: 16px; background: #fff }
.progress { color:#76839b; margin-bottom: 8px }
.progress-bar { height:8px; border-radius:999px; background:#e5e7eb; overflow:hidden; margin: 8px 0 14px }
.progress-fill { height:100%; background:#3b82f6; transition: width .3s ease }
</style>
