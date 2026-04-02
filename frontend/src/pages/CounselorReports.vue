<template>
  <div class="page counselor-page" v-loading="loading">
    <el-card>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索学生/报告" style="width:220px" @input="applyFilter" />
        <el-select v-model="scoreFilter" style="width:160px" @change="applyFilter">
          <el-option label="全部分段" value="all" />
          <el-option label="高分(85+)" value="high" />
          <el-option label="中分(60-84)" value="mid" />
          <el-option label="低分(<60)" value="low" />
        </el-select>
        <el-button @click="load">刷新</el-button>
        <el-button @click="exportCurrent">导出</el-button>
      </div>

      <div class="layout">
        <aside class="list-col">
          <div
            v-for="row in filteredList"
            :key="row.id"
            :class="['report-item', { active: currentReport?.id === row.id }]"
            @click="selectReport(row)"
          >
            <div class="line-1">
              <span>{{ row.studentName || `学生${row.studentId || '-'}` }}</span>
              <b :class="scoreClass(row.totalScore)">{{ row.totalScore ?? '--' }}分</b>
            </div>
            <div class="line-2">{{ row.testTime || row.createdAt || '-' }}</div>
          </div>
          <el-empty v-if="!filteredList.length" description="暂无报告" />
        </aside>

        <section class="detail-col" v-if="currentReport">
          <h3>报告详情预览</h3>
          <div ref="radarRef" class="radar"></div>

          <h4>专业分析编辑区</h4>
          <div class="editor-toolbar">
            <el-button size="small" @click="insertSnippet('【评估结论】\n')">结论模板</el-button>
            <el-button size="small" @click="insertSnippet('【建议方案】\n')">建议模板</el-button>
            <el-button size="small" @click="insertSnippet('• ')">列表</el-button>
          </div>
          <el-input v-model="analysisText" type="textarea" :rows="10" placeholder="输入专业分析..." />
          <div class="save-row">
            <el-button type="primary" @click="saveAnalysis">保存分析</el-button>
          </div>
        </section>

        <section class="detail-col" v-else>
          <el-empty description="请选择左侧报告" />
        </section>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { nextTick, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const loading = ref(false)
const list = ref([])
const filteredList = ref([])
const studentId = ref(route.query.studentId || '')
const keyword = ref('')
const scoreFilter = ref('all')

const analysisText = ref('')
const currentReportId = ref(0)
const currentReport = ref(null)
const radarRef = ref(null)
let radar = null

const load = async () => {
  loading.value = true
  try {
    const sid = Number(studentId.value || 0)
    const res = await axios.get('/api/counselor/reports', { params: sid > 0 ? { studentId: sid } : {} })
    const body = res.data || {}
    list.value = body.data || body
    applyFilter()
    if (!currentReport.value && filteredList.value.length) {
      selectReport(filteredList.value[0])
    }
  } finally {
    loading.value = false
  }
}

const selectReport = async (row) => {
  currentReport.value = row
  currentReportId.value = row.id
  analysisText.value = row.analysis || ''
  await nextTick()
  renderRadar(row)
}

const saveAnalysis = async () => {
  await axios.post(`/api/counselor/reports/${currentReportId.value}/analysis`, { analysis: analysisText.value })
  window.$message?.success('分析备注已保存')
  if (currentReport.value) currentReport.value.analysis = analysisText.value
  load()
}

const applyFilter = () => {
  filteredList.value = list.value.filter((row) => {
    const score = Number(row.totalScore || 0)
    const textOk = !keyword.value || `${row.studentName || ''}${row.id || ''}`.includes(keyword.value.trim())
    const scoreOk = scoreFilter.value === 'all'
      || (scoreFilter.value === 'high' && score >= 85)
      || (scoreFilter.value === 'mid' && score >= 60 && score < 85)
      || (scoreFilter.value === 'low' && score < 60)
    return textOk && scoreOk
  })
}

const scoreClass = (score) => {
  const s = Number(score || 0)
  if (s >= 85) return 'score-high'
  if (s >= 60) return 'score-mid'
  return 'score-low'
}

const renderRadar = (row) => {
  if (!radarRef.value || !row) return
  let dims = row.dimensionScores || row.dimensionScore || {}
  if (typeof dims === 'string') {
    try {
      dims = JSON.parse(dims)
    } catch (_) {
      dims = {}
    }
  }
  const entries = Object.entries(dims || {})
  const data = entries.length ? entries : [
    ['情绪稳定', 70], ['人际关系', 65], ['学习压力', 60], ['自我认同', 75], ['适应能力', 68]
  ]
  const indicator = data.map(([name]) => ({ name, max: 100 }))
  const values = data.map(([, score]) => Number(score) || 0)

  if (!radar) radar = echarts.init(radarRef.value)
  radar.setOption({
    radar: {
      indicator,
      radius: '60%',
      splitArea: { areaStyle: { color: ['#f8fafc', '#f1f5f9'] } }
    },
    series: [{
      type: 'radar',
      areaStyle: { color: 'rgba(167,139,250,0.28)' },
      lineStyle: { color: '#a78bfa' },
      data: [{ value: values, name: '维度得分' }]
    }]
  })
}

const insertSnippet = (txt) => {
  analysisText.value = `${analysisText.value || ''}${txt}`
}

const exportCurrent = () => {
  if (!currentReport.value) return
  const blob = new Blob([JSON.stringify(currentReport.value, null, 2)], { type: 'application/json;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `report-${currentReport.value.id}.json`
  a.click()
  URL.revokeObjectURL(a.href)
}

onMounted(load)
</script>

<style scoped>
.page { max-width:1200px; margin:0 auto }
.toolbar { display:flex; gap:8px; flex-wrap:wrap; margin-bottom: 14px; }
.layout {
  display:grid;
  grid-template-columns: 320px 1fr;
  gap: 14px;
  min-height: 620px;
}
.list-col {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 8px;
  overflow: auto;
}
.report-item {
  border: 1px solid transparent;
  border-radius: 12px;
  padding: 10px;
  cursor: pointer;
  margin-bottom: 8px;
}
.report-item:hover { background: var(--color-bg-tertiary); }
.report-item.active { border-color: var(--color-counselor); background: #f5f3ff; }
.line-1 { display:flex; justify-content:space-between; align-items:center; color: var(--color-text-primary); }
.line-2 { font-size: 12px; color: var(--color-text-tertiary); margin-top: 4px; }
.score-high { color: #10b981; }
.score-mid { color: #f59e0b; }
.score-low { color: #ef4444; }

.detail-col {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 14px;
}

.radar {
  height: 280px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  margin: 10px 0 14px;
}

.editor-toolbar { display:flex; gap:8px; flex-wrap:wrap; margin-bottom: 8px; }
.save-row { margin-top: 10px; }

@media (max-width: 960px) {
  .layout { grid-template-columns: 1fr; }
}
</style>
