<template>
  <div class="page admin-page" v-loading="loading">
    <div class="cards">
      <el-card class="stat-card" v-for="item in cardList" :key="item.label">
        <div class="label">{{ item.label }}</div>
        <div class="value-row">
          <div class="value">{{ item.value }}</div>
          <div class="ratio" :class="item.trend >= 0 ? 'up' : 'down'">{{ item.trend >= 0 ? '↗' : '↘' }} {{ Math.abs(item.trend) }}%</div>
        </div>
        <div class="sparkline"></div>
      </el-card>
    </div>

    <div class="chart-grid">
      <el-card>
        <h3>近30天趋势</h3>
        <div ref="trendRef" class="chart"></div>
      </el-card>
      <el-card>
        <h3>用户角色分布</h3>
        <div ref="pieRef" class="chart"></div>
      </el-card>
    </div>

    <el-card style="margin-top: 14px">
      <div class="board-row">
        <div class="todo">
          <h3>待办事项</h3>
          <div class="todo-item" v-for="item in todoList" :key="item">{{ item }}</div>
        </div>
        <div class="notice">
          <h3>系统公告</h3>
          <div class="notice-gallery">
            <div class="notice-image card-a"></div>
            <div class="notice-image card-b"></div>
          </div>
          <div class="todo-item" v-for="item in noticeList" :key="item">{{ item }}</div>
        </div>
      </div>

      <div class="quick-actions" style="margin-top:12px">
        <el-button @click="go('/admin/students')">学生管理</el-button>
        <el-button @click="go('/admin/counselors')">咨询师管理</el-button>
        <el-button @click="go('/admin/scales')">量表题目管理</el-button>
        <el-button @click="go('/admin/appointments')">预约订单管理</el-button>
        <el-button type="warning" @click="go('/admin/articles')">文章审核</el-button>
        <el-button type="primary" @click="go('/admin/profile')">个人中心</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const stats = ref({
  studentCount: 0,
  counselorCount: 0,
  assessmentCount: 0,
  appointmentCount: 0,
  pendingArticleCount: 0
})
const trendRef = ref(null)
const pieRef = ref(null)
let trendChart = null
let pieChart = null

const todoList = ['处理待审核文章', '核对异常预约数据', '同步咨询师资质复核结果']
const noticeList = ['系统将于周五晚进行维护', '新增文章审核模板功能已上线', '请定期导出核心报表备份']

const cardList = computed(() => [
  { label: '学生', value: stats.value.studentCount || 0, trend: 6 },
  { label: '咨询师', value: stats.value.counselorCount || 0, trend: 3 },
  { label: '报告', value: stats.value.assessmentCount || 0, trend: 12 },
  { label: '预约', value: stats.value.appointmentCount || 0, trend: -2 },
  { label: '待审核文章', value: stats.value.pendingArticleCount || 0, trend: 9 }
])

const go = (path) => router.push(path)

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/dashboard')
    const body = res?.data || {}
    stats.value = body.data || body
    await nextTick()
    renderCharts()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '控制台数据加载失败')
  } finally {
    loading.value = false
  }
}

const renderCharts = () => {
  if (trendRef.value) {
    if (!trendChart) trendChart = echarts.init(trendRef.value)
    const days = Array.from({ length: 30 }).map((_, i) => `${i + 1}`)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['新增用户', '报告数'] },
      xAxis: { type: 'category', data: days },
      yAxis: { type: 'value' },
      series: [
        { type: 'line', smooth: true, name: '新增用户', data: days.map((_, i) => 20 + Math.round(8 * Math.sin(i / 5))), itemStyle: { color: '#f59e0b' } },
        { type: 'bar', name: '报告数', data: days.map((_, i) => 35 + Math.round(10 * Math.cos(i / 6))), itemStyle: { color: '#60a5fa' } }
      ]
    })
  }
  if (pieRef.value) {
    if (!pieChart) pieChart = echarts.init(pieRef.value)
    const total = (stats.value.studentCount || 0) + (stats.value.counselorCount || 0) + 1
    pieChart.setOption({
      title: { text: `${total}`, subtext: '总用户', left: 'center', top: '40%', textStyle: { fontSize: 28 } },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['56%', '76%'],
        data: [
          { name: '学生', value: stats.value.studentCount || 0 },
          { name: '咨询师', value: stats.value.counselorCount || 0 },
          { name: '管理员', value: 1 }
        ]
      }]
    })
  }
}

onMounted(load)
</script>

<style scoped>
.page {
  max-width: 1180px;
  margin: 0 auto;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.stat-card .label {
  color: #6b7280;
  font-size: 13px;
}

.value-row { display:flex; justify-content:space-between; align-items:flex-end; margin-top: 8px; }

.stat-card .value {
  color: #111827;
  font-size: 28px;
  font-weight: 700;
}

.ratio { font-size: 12px; }
.ratio.up { color: #10b981; }
.ratio.down { color: #ef4444; }

.sparkline {
  margin-top: 10px;
  height: 34px;
  border-radius: 8px;
  background: linear-gradient(90deg, #fef3c7, #fde68a);
}

.chart-grid {
  margin-top: 14px;
  display:grid;
  grid-template-columns: 2fr 1fr;
  gap: 12px;
}

.chart { height: 310px; }

.board-row {
  display:grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.todo-item {
  height: 40px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  display:flex;
  align-items:center;
  padding: 0 10px;
  margin-top: 8px;
  background: #fff;
}

.notice-gallery {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin: 10px 0 12px;
}

.notice-image {
  min-height: 140px;
  border-radius: 14px;
  border: 1px solid var(--color-border);
  overflow: hidden;
  background-size: cover;
  background-position: center;
  box-shadow: var(--shadow-sm);
}

.notice-image.card-a {
  background-image:
    linear-gradient(135deg, rgba(255,255,255,0.15), rgba(16, 185, 129, 0.10)),
    url('/images/hand.jpg');
}

.notice-image.card-b {
  background-image:
    linear-gradient(135deg, rgba(255,255,255,0.15), rgba(91, 124, 245, 0.10)),
    url('/images/manyhands.jpg');
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 960px) {
  .chart-grid,
  .board-row { grid-template-columns: 1fr; }

  .notice-gallery {
    grid-template-columns: 1fr;
  }
}
</style>
