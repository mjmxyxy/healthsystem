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
      <div id="chart" style="height:300px;margin-bottom:16px"></div>
      <el-table :data="reports" style="width:100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="createdAt" label="时间" />
        <el-table-column prop="totalScore" label="总分" />
        <el-table-column prop="avgScore" label="平均分" />
        <el-table-column prop="level" label="等级" />
        <el-table-column label="操作">
          <template #default="{row}">
            <el-button size="small" @click="view(row.id)">查看</el-button>
            <el-button size="small" :loading="exportingId === row.id" @click="exportPdf(row.id)">导出 PDF</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && reports.length === 0" description="暂无测评记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'

const reports = ref([])
const chart = ref(null)
const loading = ref(false)
const exportingId = ref(null)
const sortOrder = ref('timeDesc')
const router = useRouter()

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
  const labels = reports.value.map(r=>r.createdAt)
  const vals = reports.value.map(r=>r.totalScore)
  chart.value.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value' },
    series: [{ data: vals, type: 'line', smooth: true }]
  })
}

const applySort = () => {
  const arr = [...reports.value]
  if (sortOrder.value === 'timeAsc') arr.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
  if (sortOrder.value === 'timeDesc') arr.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
  if (sortOrder.value === 'scoreAsc') arr.sort((a, b) => a.totalScore - b.totalScore)
  if (sortOrder.value === 'scoreDesc') arr.sort((a, b) => b.totalScore - a.totalScore)
  reports.value = arr
  renderChart()
}

const view = (id) => router.push(`/student/assessment/${id}`)

const exportPdf = async (id) => {
  exportingId.value = id
  try {
    const r = await axios.get(`/api/scale/report/${id}`)
    const w = window.open('', '_blank')
    w.document.write('<pre>' + JSON.stringify(r.data, null, 2) + '</pre>')
    w.document.close()
    w.print()
  } finally {
    exportingId.value = null
  }
}

const goHome = () => router.push('/student/home')

onMounted(load)
</script>

<style scoped>
.page { max-width:960px; margin: 24px auto }
#chart { background: #fff }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px }
.head-actions { display:flex; gap:12px; align-items:center }
</style>
