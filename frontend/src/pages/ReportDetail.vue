<template>
  <div class="page">
    <el-card v-loading="loading">
      <div style="display:flex;justify-content:space-between;align-items:center; gap: 8px; flex-wrap: wrap">
        <h2>测评报告详情</h2>
        <div>
          <el-button @click="back">返回历史</el-button>
          <el-button @click="goHome">回到首页</el-button>
          <el-button type="primary" :loading="exporting" @click="exportPdf">导出 PDF</el-button>
        </div>
      </div>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="量表">{{ report.scaleName }}</el-descriptions-item>
        <el-descriptions-item label="总分">{{ report.totalScore }}</el-descriptions-item>
        <el-descriptions-item label="标准分">{{ report.avgScore }}</el-descriptions-item>
        <el-descriptions-item label="等级">{{ report.level }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ report.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="建议">{{ report.suggestion }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />
      <h3>维度解析</h3>
      <el-table :data="dimensionRows" style="width: 100%">
        <el-table-column prop="dimension" label="维度" />
        <el-table-column prop="score" label="分值" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const report = ref({})
const loading = ref(false)
const exporting = ref(false)

const dimensionRows = computed(() => {
  const raw = report.value.dimensionScores
  if (!raw) return []
  let obj = raw
  if (typeof raw === 'string') {
    try {
      obj = JSON.parse(raw)
    } catch (_) {
      obj = {}
    }
  }
  return Object.entries(obj || {}).map(([dimension, score]) => ({ dimension, score }))
})

const load = async () => {
  const id = Number(route.params.id)
  loading.value = true
  try {
    const res = await axios.get(`/api/scale/report/${id}`)
    const body = res.data || {}
    report.value = body.data || body
  } finally {
    loading.value = false
  }
}

const back = () => router.push('/student/assessments')
const goHome = () => router.push('/student/home')

const exportPdf = async () => {
  exporting.value = true
  try {
    const w = window.open('', '_blank')
    w.document.write('<pre>' + JSON.stringify(report.value, null, 2) + '</pre>')
    w.document.close()
    w.print()
  } finally {
    exporting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page { max-width:960px; margin:24px auto; padding:0 16px }
</style>
