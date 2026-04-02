<template>
  <div class="page counselor-page">
    <div class="crumb">工作台 / 仪表板</div>

    <div class="cards">
      <el-card v-for="item in statCards" :key="item.key" class="stat-card">
        <div class="left-icon" :style="{ background: item.bg }">{{ item.icon }}</div>
        <div class="main">
          <div class="label">{{ item.label }}</div>
          <div class="value">{{ item.value }}</div>
          <div class="trend" :class="item.trend >= 0 ? 'up' : 'down'">
            {{ item.trend >= 0 ? '↗' : '↘' }} {{ Math.abs(item.trend) }}%
          </div>
        </div>
      </el-card>
    </div>

    <el-card v-loading="loading" style="margin-top:16px">
      <div class="head">
        <h2>今日日程</h2>
        <el-button @click="goAppointments">进入预约管理</el-button>
      </div>

      <div class="timeline" v-if="todaySchedules.length">
        <div v-for="item in todaySchedules" :key="item.id" class="line-item">
          <div class="time">{{ item.time }}</div>
          <div class="dot" :class="item.stateClass"></div>
          <div class="content">
            <div class="title">{{ item.studentName }} · {{ item.statusText }}</div>
            <div class="sub">{{ item.fullTime }}</div>
          </div>
          <el-tooltip :content="item.countdownText" placement="top">
            <el-button size="small" type="primary" plain @click="openChat(item)">进入咨询</el-button>
          </el-tooltip>
        </div>
      </div>

      <el-empty v-else description="今日暂无预约" />
    </el-card>

    <el-card style="margin-top:16px">
      <div class="head">
        <h2>待办事项</h2>
      </div>
      <div class="todo-list">
        <div v-for="todo in todos" :key="todo.id" class="todo-item">
          <span class="priority" :class="todo.level">{{ todo.levelText }}</span>
          <span>{{ todo.text }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const stats = ref({ pendingAppointments: 0, todayConfirmed: 0, upcomingConsultations: 0, recentAppointments: [] })

const statCards = computed(() => [
  {
    key: 'pending',
    label: '待处理',
    value: stats.value.pendingAppointments || 0,
    icon: '待',
    bg: 'rgba(245, 158, 11, 0.18)',
    trend: 12
  },
  {
    key: 'today',
    label: '今日咨询',
    value: stats.value.todayConfirmed || 0,
    icon: '今',
    bg: 'rgba(59, 130, 246, 0.15)',
    trend: 8
  },
  {
    key: 'future',
    label: '后续跟进',
    value: stats.value.upcomingConsultations || 0,
    icon: '续',
    bg: 'rgba(167, 139, 250, 0.18)',
    trend: 15
  }
])

const todaySchedules = computed(() => {
  const list = Array.isArray(stats.value.recentAppointments) ? stats.value.recentAppointments : []
  const now = new Date()
  return list
    .filter((item) => {
      const t = new Date(item.startTime || item.apptTime || item.time || '')
      return !Number.isNaN(t.getTime()) && t.toDateString() === now.toDateString()
    })
    .map((item, idx) => {
      const t = new Date(item.startTime || item.apptTime || item.time)
      const diff = Math.round((t.getTime() - now.getTime()) / 60000)
      const statusVal = Number(item.status)
      const statusText = ({ 0: '待确认', 1: '进行中', 2: '已完成', 3: '已取消' }[statusVal] || '待处理')
      const stateClass = statusVal === 2 ? 'done' : statusVal === 1 ? 'doing' : 'wait'
      return {
        id: item.id || idx,
        studentName: item.studentName || '学生',
        statusText,
        stateClass,
        time: `${String(t.getHours()).padStart(2, '0')}:${String(t.getMinutes()).padStart(2, '0')}`,
        fullTime: t.toLocaleString('zh-CN', { hour12: false }),
        countdownText: diff > 0 ? `剩余${diff}分钟` : '可立即进入',
        studentId: item.studentId
      }
    })
})

const todos = ref([
  { id: 1, level: 'high', levelText: '高', text: '完成高风险学生周报复核' },
  { id: 2, level: 'mid', levelText: '中', text: '确认明日预约时段并发送提醒' },
  { id: 3, level: 'low', levelText: '低', text: '更新本周心理科普文章草稿' }
])

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/counselor/dashboard')
    const body = res.data || {}
    stats.value = body.data || body
  } finally {
    loading.value = false
  }
}

const goAppointments = () => router.push('/counselor/appointments')
const openChat = (row) => {
  const studentId = row.studentId
  if (!studentId) return
  router.push(`/counselor/chat/${studentId}`)
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; }
.crumb { color: var(--color-text-tertiary); font-size: 13px; margin-bottom: 10px; }
.cards { display:grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; }
.stat-card :deep(.el-card__body) { display:flex; align-items:center; gap:14px; }
.left-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display:flex;
  align-items:center;
  justify-content:center;
  color: #5b21b6;
  font-weight: 700;
}
.main { flex: 1; }
.label { color: var(--color-text-secondary); font-size: 13px; }
.value { color:#111827; font-size:32px; font-weight:700; margin-top:6px; line-height: 1; }
.trend { margin-top: 8px; font-size: 12px; }
.trend.up { color: #10b981; }
.trend.down { color: #ef4444; }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }

.timeline {
  position: relative;
  padding-left: 8px;
}

.line-item {
  display:grid;
  grid-template-columns: 70px 20px 1fr auto;
  gap: 10px;
  align-items:center;
  min-height: 56px;
  border-left: 1px solid var(--color-border);
  padding: 6px 0 6px 12px;
}

.time { color: var(--color-text-secondary); font-size: 13px; }

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot.wait { background: #f59e0b; }
.dot.done { background: #10b981; }
.dot.doing { background: #3b82f6; }

.title { font-weight: var(--font-weight-semibold); color: var(--color-text-primary); }
.sub { font-size: 12px; color: var(--color-text-tertiary); margin-top: 2px; }

.todo-list { display:grid; gap: 10px; }
.todo-item {
  display:flex;
  align-items:center;
  gap: 10px;
  height: 46px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 0 12px;
  background: #fff;
}

.priority {
  display:inline-flex;
  align-items:center;
  justify-content:center;
  width: 24px;
  height: 24px;
  border-radius: 999px;
  color: #fff;
  font-size: 12px;
}

.priority.high { background: #ef4444; }
.priority.mid { background: #f59e0b; }
.priority.low { background: #10b981; }

@media (max-width: 960px) {
  .cards { grid-template-columns: 1fr; }
  .line-item { grid-template-columns: 56px 16px 1fr; }
  .line-item :deep(.el-button) { grid-column: 3; justify-self: start; margin-top: 4px; }
}
</style>
