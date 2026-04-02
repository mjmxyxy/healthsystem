<template>
  <div class="page">
    <el-card v-loading="loading">
      <div class="head">
        <h2>测评历史</h2>
        <div class="head-actions">
          <el-select v-model="sortOrder" style="width:180px" @change="applySort">
            <el-option label="按时间降序" value="timeDesc" />
            <el-option label="按时间升序" value="timeAsc" />
            <el-option label="按总分降序" value="scoreDesc" />
            <el-option label="按总分升序" value="scoreAsc" />
          </el-select>
          <el-button @click="goHome">返回首页</el-button>
        </div>
      </div>

      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">总测评数</div>
          <div class="stat-value">{{ stats.totalCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">本月测评</div>
          <div class="stat-value">{{ stats.monthCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">平均分</div>
          <div class="stat-value">{{ stats.avgScore }}</div>
        </div>
      </div>

      <div class="chart-wrap">
        <div class="section-title">情绪趋势（近30天）</div>
        <div id="chart" class="chart"></div>
      </div>

      <div class="calendar-wrap">
        <div class="calendar-head">
          <div class="section-title">情绪日历</div>
          <div class="cal-actions">
            <el-button size="small" @click="prevMonth">上个月</el-button>
            <span class="month-label">{{ monthLabel }}</span>
            <el-button size="small" @click="nextMonth">下个月</el-button>
          </div>
        </div>
        <div class="week-row">
          <span v-for="w in weekLabels" :key="w">{{ w }}</span>
        </div>
        <div class="day-grid">
          <div
            class="day-cell"
            v-for="d in calendarCells"
            :key="d.key"
            :class="{ empty: !d.date, today: d.isToday }"
            :title="d.tip"
          >
            <span v-if="d.date" class="day-number">{{ d.day }}</span>
            <span v-if="d.date && d.dot" class="dot" :class="d.dot"></span>
          </div>
        </div>
      </div>

      <div class="list-wrap">
        <div class="section-title">测评记录</div>
        <div class="record-list">
          <div class="record-item" v-for="row in reports" :key="row.id">
            <div class="date-block">
              <div class="day">{{ dayPart(row.createdAt) }}</div>
              <div class="month">{{ monthPart(row.createdAt) }}</div>
            </div>
            <div class="record-main">
              <div class="record-title">{{ row.scaleName || '心理测评量表' }}</div>
              <div class="record-sub">
                <span class="level-tag" :class="levelClass(row.level)">{{ levelText(row.level) }}</span>
                <span class="record-time">{{ formatDateTime(row.createdAt) }}</span>
              </div>
            </div>
            <div class="record-right">
              <div class="score" :class="scoreClass(row.level)">{{ row.totalScore }}</div>
              <el-button text @click="view(row.id)">查看报告 →</el-button>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && reports.length === 0" description="暂无测评记录" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'

const reports = ref([])
const chart = ref(null)
const loading = ref(false)
const sortOrder = ref('timeDesc')
const router = useRouter()
const weekLabels = ['一', '二', '三', '四', '五', '六', '日']
const currentMonth = ref(new Date())

const load = async () => {
  loading.value = true
  try {
    const userId = Number(localStorage.getItem('user_id') || 0)
    const r = await axios.get('/api/scale/report', { params: { userId } })
    const body = r.data || {}
    const list = body.data || body
    reports.value = (Array.isArray(list) ? list : []).map(x => ({
      id: x.id,
      totalScore: Number(x.totalScore ?? x.total_score ?? 0),
      avgScore: Number(x.avgScore ?? x.avg_score ?? 0),
      level: x.level,
      scaleName: x.scaleName ?? x.scale_name ?? x.scaleCode ?? x.scale_code,
      createdAt: x.createdAt ?? x.created_at
    }))
    applySort()
  } finally {
    loading.value = false
  }
}

const renderChart = () => {
  const dom = document.getElementById('chart')
  if (!dom) return
  if (!chart.value) chart.value = echarts.init(dom)
  const filtered = last30DaysData.value
  const labels = filtered.map(r => formatDay(r.createdAt))
  const vals = filtered.map(r => r.totalScore)
  const gradient = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    { offset: 0, color: 'rgba(91, 124, 245, 0.2)' },
    { offset: 1, color: 'rgba(91, 124, 245, 0.02)' }
  ])
  chart.value.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 24, right: 16, top: 16, bottom: 24, containLabel: true },
    xAxis: {
      type: 'category',
      data: labels,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#6b7280', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#eef2f7' } },
      axisLabel: { color: '#6b7280', fontSize: 12 }
    },
    series: [{
      data: vals,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color: '#5B7CF5', width: 3 },
      itemStyle: { color: '#5B7CF5' },
      areaStyle: { color: gradient }
    }]
  })
}

const applySort = () => {
  const arr = [...reports.value]
  if (sortOrder.value === 'timeAsc') arr.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
  if (sortOrder.value === 'timeDesc') arr.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
  if (sortOrder.value === 'scoreAsc') arr.sort((a, b) => a.totalScore - b.totalScore)
  if (sortOrder.value === 'scoreDesc') arr.sort((a, b) => b.totalScore - a.totalScore)
  reports.value = arr
  nextTick(() => renderChart())
}

const view = (id) => router.push(`/student/assessment/${id}`)

const goHome = () => router.push('/student/home')

const parseDate = (v) => {
  const d = new Date(v)
  return Number.isNaN(d.getTime()) ? null : d
}

const formatDateTime = (v) => {
  const d = parseDate(v)
  return d ? d.toLocaleString('zh-CN', { hour12: false }) : String(v || '-')
}

const formatDay = (v) => {
  const d = parseDate(v)
  return d ? `${d.getMonth() + 1}/${d.getDate()}` : '-'
}

const dayPart = (v) => {
  const d = parseDate(v)
  return d ? String(d.getDate()).padStart(2, '0') : '--'
}

const monthPart = (v) => {
  const d = parseDate(v)
  return d ? `${d.getMonth() + 1}月` : '--'
}

const monthLabel = computed(() => {
  const y = currentMonth.value.getFullYear()
  const m = currentMonth.value.getMonth() + 1
  return `${y}年${m}月`
})

const stats = computed(() => {
  const totalCount = reports.value.length
  const now = new Date()
  const y = now.getFullYear()
  const m = now.getMonth()
  const monthCount = reports.value.filter((r) => {
    const d = parseDate(r.createdAt)
    return d && d.getFullYear() === y && d.getMonth() === m
  }).length
  const avg = totalCount === 0 ? 0 : reports.value.reduce((s, r) => s + Number(r.totalScore || 0), 0) / totalCount
  return { totalCount, monthCount, avgScore: avg.toFixed(1) }
})

const last30DaysData = computed(() => {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 30)
  return [...reports.value]
    .filter((r) => {
      const d = parseDate(r.createdAt)
      return d && d >= start && d <= end
    })
    .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
})

const levelText = (level) => {
  const text = String(level || '').toLowerCase()
  if (text.includes('正常') || text.includes('normal')) return '正常'
  if (text.includes('轻') || text.includes('mild')) return '轻度'
  if (text.includes('中') || text.includes('moderate')) return '中度'
  if (text.includes('重') || text.includes('severe')) return '重度'
  return String(level || '未知')
}

const levelClass = (level) => {
  const l = levelText(level)
  if (l === '正常') return 'normal'
  if (l === '轻度') return 'light'
  if (l === '中度') return 'middle'
  if (l === '重度') return 'heavy'
  return 'normal'
}

const scoreClass = (level) => {
  const l = levelText(level)
  if (l === '正常') return 'score-normal'
  if (l === '轻度') return 'score-light'
  if (l === '中度') return 'score-middle'
  if (l === '重度') return 'score-heavy'
  return 'score-normal'
}

const dotByLevel = (level) => {
  const l = levelText(level)
  if (l === '正常') return 'green'
  if (l === '轻度') return 'yellow'
  return 'red'
}

const dailyMap = computed(() => {
  const map = new Map()
  for (const r of reports.value) {
    const d = parseDate(r.createdAt)
    if (!d) continue
    const key = `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()}`
    if (!map.has(key) || new Date(map.get(key).createdAt).getTime() < d.getTime()) {
      map.set(key, r)
    }
  }
  return map
})

const calendarCells = computed(() => {
  const y = currentMonth.value.getFullYear()
  const m = currentMonth.value.getMonth()
  const first = new Date(y, m, 1)
  const totalDays = new Date(y, m + 1, 0).getDate()
  const firstDay = (first.getDay() + 6) % 7
  const cells = []
  const now = new Date()

  for (let i = 0; i < firstDay; i++) {
    cells.push({ key: `empty-${i}`, date: null })
  }
  for (let d = 1; d <= totalDays; d++) {
    const key = `${y}-${m + 1}-${d}`
    const report = dailyMap.value.get(key)
    const isToday = y === now.getFullYear() && m === now.getMonth() && d === now.getDate()
    const tip = report ? `${formatDateTime(report.createdAt)}\n${report.scaleName || '心理测评量表'}\n总分: ${report.totalScore}` : `${y}-${m + 1}-${d}`
    cells.push({
      key,
      day: d,
      date: key,
      dot: report ? dotByLevel(report.level) : '',
      isToday,
      tip
    })
  }
  while (cells.length < 35) {
    cells.push({ key: `tail-${cells.length}`, date: null })
  }
  return cells.slice(0, 35)
})

const prevMonth = () => {
  const d = new Date(currentMonth.value)
  d.setMonth(d.getMonth() - 1)
  currentMonth.value = d
}

const nextMonth = () => {
  const d = new Date(currentMonth.value)
  d.setMonth(d.getMonth() + 1)
  currentMonth.value = d
}

onMounted(load)
</script>

<style scoped>
.page { max-width:1080px; margin: 0 auto }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px }
.head-actions { display:flex; gap:12px; align-items:center }

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.stat-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-bg-primary);
  padding: 12px;
}

.stat-label {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.stat-value {
  margin-top: 8px;
  color: var(--color-student);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
}

.chart-wrap,
.calendar-wrap,
.list-wrap {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-bg-primary);
  padding: 12px;
  margin-bottom: 12px;
}

.section-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  margin-bottom: 10px;
}

.chart {
  height: 200px;
}

.calendar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.cal-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.month-label {
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.week-row,
.day-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}

.week-row span {
  text-align: center;
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
}

.day-cell {
  height: 44px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.day-cell.empty {
  background: var(--color-bg-secondary);
}

.day-cell.today {
  border-color: var(--color-student);
}

.day-number {
  font-size: var(--font-size-xs);
  color: var(--color-text-primary);
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 4px;
}

.dot.green { background: var(--color-success); }
.dot.yellow { background: var(--color-warning); }
.dot.red { background: var(--color-danger); }

.record-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.record-item {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 10px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.date-block {
  width: 58px;
  border-radius: var(--radius-md);
  background: var(--color-bg-tertiary);
  text-align: center;
  padding: 8px 0;
}

.date-block .day {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  line-height: 1.2;
}

.date-block .month {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.record-main {
  flex: 1;
}

.record-title {
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
  font-weight: var(--font-weight-semibold);
}

.record-sub {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.record-time {
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
}

.level-tag {
  font-size: var(--font-size-xs);
  border-radius: 999px;
  padding: 2px 8px;
}

.level-tag.normal { background: #10b981; color: #fff; }
.level-tag.light { background: #fef3c7; color: #92400e; }
.level-tag.middle { background: #f59e0b; color: #fff; }
.level-tag.heavy { background: #ef4444; color: #fff; }

.record-right {
  min-width: 92px;
  text-align: right;
}

.score {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  line-height: 1.1;
}

.score-normal { color: #10b981; }
.score-light { color: #b45309; }
.score-middle { color: #f59e0b; }
.score-heavy { color: #ef4444; }

@media (max-width: 860px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
  .record-item {
    flex-wrap: wrap;
  }
  .record-right {
    margin-left: auto;
  }
}
</style>
