<template>
  <div class="page">
    <el-card v-loading="loading" class="test-card">
      <div class="head-row">
        <div>
          <h2>{{ scaleName }}</h2>
          <p class="hint">共 {{ questions.length }} 题，已作答 {{ answeredCount }} 题</p>
        </div>
        <el-button @click="backToScales">返回量表</el-button>
      </div>

      <div class="progress-bar" v-if="questions.length > 0">
        <div class="progress-fill" :style="{ width: ((answeredCount / questions.length) * 100).toFixed(0) + '%' }" />
      </div>

      <el-empty v-if="!loading && questions.length === 0" description="暂无题目" />

      <div v-else class="question-wrap">
        <div class="question card-hover">
          <div class="progress">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</div>
          <p class="question-text">{{ currentQuestion.seq || currentIndex + 1 }}. {{ currentQuestion.text }}</p>

          <div class="option-list">
            <label
              v-for="opt in parseOptions(currentQuestion.options)"
              :key="opt.value"
              :class="['option-item', { active: answers[currentQuestion.id] === opt.value }]"
            >
              <input type="radio" :value="opt.value" v-model="answers[currentQuestion.id]" />
              <span>{{ opt.label }}</span>
            </label>
          </div>
        </div>

        <div class="bottom-bar">
          <div class="index-pill">{{ currentIndex + 1 }} / {{ questions.length || 0 }}</div>
          <div class="actions">
            <el-button :disabled="currentIndex === 0" @click="prevQuestion">上一题</el-button>
            <el-button :disabled="currentIndex >= questions.length - 1" @click="nextQuestion">下一题</el-button>
            <el-button type="primary" :disabled="!allAnswered || submitting" :loading="submitting" @click="submit">提交测评</el-button>
          </div>
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
.page { max-width: 980px; margin: 0 auto; }
.test-card { border-radius: 16px; }
.head-row {
  display:flex;
  justify-content:space-between;
  align-items:flex-start;
  gap: 12px;
  flex-wrap: wrap;
}
.hint { color: var(--color-text-secondary); margin-top: 4px; }

.progress-bar {
  margin: 12px 0 16px;
  height: 4px;
  border-radius: 999px;
  background: #d1d5db;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--color-student);
  transition: width .25s ease;
}

.question-wrap {
  margin-top: 8px;
}

.question {
  border-radius: 14px;
  border: 1px solid var(--color-border);
  padding: 22px;
  background: #fff;
}

.progress {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-sm);
  margin-bottom: 8px;
}

.question-text {
  font-size: 24px;
  line-height: 1.45;
  margin: 0 0 18px;
  color: var(--color-text-primary);
}

.option-list {
  display:grid;
  gap: 10px;
}

.option-item {
  min-height: 52px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  display:flex;
  align-items:center;
  gap: 10px;
  padding: 0 12px;
  cursor: pointer;
  transition: all .2s ease;
}

.option-item input { accent-color: var(--color-student); }

.option-item.active {
  border-color: #60a5fa;
  background: #eff6ff;
}

.bottom-bar {
  margin-top: 14px;
  display:flex;
  justify-content:space-between;
  align-items:center;
  gap: 12px;
  flex-wrap: wrap;
}

.index-pill {
  height: 36px;
  border-radius: 999px;
  background: #f3f4f6;
  padding: 0 14px;
  display:flex;
  align-items:center;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.actions {
  display:flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
