<template>
  <div class="page">
    <el-card v-loading="loading" class="report-card">
      <div class="top-row">
        <h2>测评报告详情</h2>
        <div class="btns">
          <el-button @click="back">返回历史</el-button>
          <el-button @click="goHome">回到首页</el-button>
          <el-button type="primary" :loading="exporting" @click="exportPdf">导出 PDF</el-button>
        </div>
      </div>

      <div class="score-hero">
        <div class="score-main">
          <div class="label">总分</div>
          <div class="num">{{ report.totalScore ?? '--' }}</div>
          <div class="meta">{{ report.scaleName || '心理测评' }} · {{ report.level || '待评估' }}</div>
        </div>
        <div class="score-side">
          <div>标准分：{{ report.avgScore ?? '--' }}</div>
          <div>测评时间：{{ report.createdAt || '--' }}</div>
        </div>
      </div>

      <div class="section">
        <h3>维度雷达图</h3>
        <div ref="radarRef" class="radar"></div>
      </div>

      <div class="section">
        <h3>维度明细</h3>
        <el-table :data="dimensionRows" style="width: 100%">
          <el-table-column prop="dimension" label="维度" />
          <el-table-column prop="score" label="分值" width="120" />
        </el-table>
      </div>

      <div class="section">
        <h3>分析建议</h3>
        <el-collapse>
          <el-collapse-item title="查看个性化建议" name="1">
            <p class="suggestion">{{ report.suggestion || '暂无建议，请保持规律作息并持续关注情绪变化。' }}</p>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const report = ref({})
const loading = ref(false)
const exporting = ref(false)
const radarRef = ref(null)
let radarChart = null

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
    await nextTick()
    renderRadar()
  } finally {
    loading.value = false
  }
}

const renderRadar = () => {
  if (!radarRef.value) return
  const data = dimensionRows.value
  const indicators = (data.length ? data : [{ dimension: '暂无', score: 0 }]).map((i) => ({ name: i.dimension, max: 100 }))
  const values = (data.length ? data : [{ score: 0 }]).map((i) => Number(i.score) || 0)

  if (!radarChart) {
    radarChart = echarts.init(radarRef.value)
  }

  radarChart.setOption({
    tooltip: {},
    radar: {
      indicator: indicators,
      radius: '62%',
      splitArea: { areaStyle: { color: ['#f8fafc', '#f1f5f9'] } }
    },
    series: [
      {
        type: 'radar',
        areaStyle: { color: 'rgba(59,130,246,0.25)' },
        lineStyle: { color: '#3b82f6' },
        data: [{ value: values, name: '维度得分' }]
      }
    ]
  })
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
.page { max-width: 1000px; margin: 0 auto; }
.report-card { border-radius: 16px; }

.top-row {
  display:flex;
  justify-content:space-between;
  align-items:center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.btns { display:flex; gap:8px; flex-wrap:wrap; }

.score-hero {
  border-radius: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  display:flex;
  justify-content:space-between;
  align-items:flex-end;
  gap: 16px;
  flex-wrap: wrap;
}

.label {
  color: #1e40af;
  font-size: var(--font-size-sm);
}

.num {
  margin-top: 6px;
  font-size: 52px;
  line-height: 1;
  font-weight: 700;
  color: #1e3a8a;
}

.meta {
  margin-top: 8px;
  color: #1e40af;
}

.score-side {
  color: #1e3a8a;
  display:grid;
  gap: 8px;
}

.section { margin-top: 18px; }

.radar {
  height: 340px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: #fff;
}

.suggestion {
  color: var(--color-text-secondary);
  line-height: 1.8;
  margin: 0;
}
</style>
