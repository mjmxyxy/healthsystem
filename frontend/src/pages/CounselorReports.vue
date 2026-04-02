<template>
  <div class="page">
    <el-card v-loading="loading">
      <div class="head">
        <h2>测评报告查看</h2>
        <div>
          <el-input v-model="studentId" placeholder="按学生ID筛选" style="width:180px; margin-right:8px" />
          <el-button @click="load">查询</el-button>
        </div>
      </div>

      <el-table :data="list">
        <el-table-column prop="id" label="报告ID" width="90" />
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="testTime" label="测评时间" width="180" />
        <el-table-column prop="totalScore" label="总分" width="90" />
        <el-table-column prop="level" label="等级" width="90" />
        <el-table-column prop="analysis" label="专业分析备注" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" @click="preview(row)">查看内容</el-button>
            <el-button size="small" type="primary" @click="editAnalysis(row)">编辑分析</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showPreview" title="报告详情" width="700px">
      <pre class="detail">{{ previewText }}</pre>
    </el-dialog>

    <el-dialog v-model="showAnalysis" title="编辑专业分析" width="620px">
      <el-input v-model="analysisText" type="textarea" :rows="8" placeholder="输入专业分析建议" />
      <template #footer>
        <el-button @click="showAnalysis = false">取消</el-button>
        <el-button type="primary" @click="saveAnalysis">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const loading = ref(false)
const list = ref([])
const studentId = ref(route.query.studentId || '')

const showPreview = ref(false)
const previewText = ref('')
const showAnalysis = ref(false)
const analysisText = ref('')
const currentReportId = ref(0)

const load = async () => {
  loading.value = true
  try {
    const sid = Number(studentId.value || 0)
    const res = await axios.get('/api/counselor/reports', { params: sid > 0 ? { studentId: sid } : {} })
    const body = res.data || {}
    list.value = body.data || body
  } finally {
    loading.value = false
  }
}

const preview = (row) => {
  previewText.value = row.report || ''
  showPreview.value = true
}

const editAnalysis = (row) => {
  currentReportId.value = row.id
  analysisText.value = row.analysis || ''
  showAnalysis.value = true
}

const saveAnalysis = async () => {
  await axios.post(`/api/counselor/reports/${currentReportId.value}/analysis`, { analysis: analysisText.value })
  window.$message?.success('分析备注已保存')
  showAnalysis.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.page { max-width:1080px; margin:0 auto }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px }
.detail { white-space: pre-wrap; word-break: break-word; background:#f9fafb; border:1px solid #e5e7eb; padding:12px; border-radius:8px; }
</style>
