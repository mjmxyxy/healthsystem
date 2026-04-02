<template>
  <div class="page counselor-page">
    <el-card v-loading="loading">
      <div class="head">
        <h2>学生管理</h2>
        <div class="ops">
          <el-input v-model="keyword" placeholder="姓名/学号搜索" clearable style="width:220px" @input="applyFilter" />
          <el-select v-model="riskFilter" placeholder="风险等级" style="width:140px" @change="applyFilter">
            <el-option label="全部风险" value="all" />
            <el-option label="低风险" value="low" />
            <el-option label="中风险" value="mid" />
            <el-option label="高风险" value="high" />
          </el-select>
          <el-select v-model="timeFilter" placeholder="最近咨询" style="width:170px" @change="applyFilter">
            <el-option label="全部时间" value="all" />
            <el-option label="近7天" value="7" />
            <el-option label="近30天" value="30" />
          </el-select>
          <el-button @click="load">刷新</el-button>
        </div>
      </div>

      <div v-if="selectedRows.length" class="batch-bar">
        已选中 {{ selectedRows.length }} 人
        <el-button size="small" @click="bulkTag('follow')">标记跟进</el-button>
        <el-button size="small" @click="bulkTag('review')">标记复评</el-button>
      </div>

      <el-table
        :data="filteredList"
        @selection-change="selectedRows = $event"
        @row-click="openDrawer"
        :row-class-name="rowClassName"
      >
        <el-table-column type="selection" width="54" />
        <el-table-column label="学生信息" min-width="260">
          <template #default="{ row }">
            <div class="student-cell">
              <div class="avatar">{{ avatarText(row.name) }}</div>
              <div>
                <div class="name">{{ row.name || '未命名学生' }}</div>
                <div class="sub">{{ row.studentNo || row.studentId || '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="90" />
        <el-table-column label="风险等级" width="120">
          <template #default="{ row }">
            <span class="risk-dot" :class="riskInfo(row).className"></span>{{ riskInfo(row).text }}
          </template>
        </el-table-column>
        <el-table-column label="测评数" width="90">
          <template #default="{ row }">
            <el-badge :value="row.reportCount || 0" class="score-badge" />
          </template>
        </el-table-column>
        <el-table-column label="最近咨询时间" min-width="170">
          <template #default="{ row }">{{ row.lastConsultTime || row.lastTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="咨询备注" min-width="220">
          <template #default="{ row }">
            <el-input
              v-if="editingId === row.studentId"
              v-model="row.note"
              size="small"
              @blur="saveInlineNote(row)"
              @keyup.enter="saveInlineNote(row)"
            />
            <div v-else class="note" @dblclick.stop="editingId = row.studentId">{{ row.note || '双击快速编辑备注' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <div class="table-actions cols-2">
              <el-button size="small" @click.stop="editingId = row.studentId">编辑备注</el-button>
              <el-button size="small" type="primary" @click="viewReports(row)">查看报告</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showNote" title="编辑学生备注" width="520px">
      <el-input v-model="noteText" type="textarea" :rows="6" placeholder="输入咨询师备注" />
      <template #footer>
        <el-button @click="showNote = false">取消</el-button>
        <el-button type="primary" @click="saveNote">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="showDrawer" title="学生档案" size="420px">
      <el-descriptions :column="1" border v-if="currentStudent">
        <el-descriptions-item label="姓名">{{ currentStudent.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentStudent.studentNo || currentStudent.studentId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ currentStudent.gender || '-' }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">{{ riskInfo(currentStudent).text }}</el-descriptions-item>
        <el-descriptions-item label="最近咨询">{{ currentStudent.lastConsultTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="咨询备注">{{ currentStudent.note || '暂无备注' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const filteredList = ref([])
const keyword = ref('')
const riskFilter = ref('all')
const timeFilter = ref('all')
const selectedRows = ref([])
const editingId = ref(0)
const showDrawer = ref(false)
const currentStudent = ref(null)
const showNote = ref(false)
const noteText = ref('')
const currentStudentId = ref(0)

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/counselor/students')
    const body = res.data || {}
    list.value = body.data || body
    applyFilter()
  } finally {
    loading.value = false
  }
}

const riskInfo = (row) => {
  const v = (row.riskLevel || row.risk || '').toString().toLowerCase()
  if (v.includes('high') || v.includes('高')) return { text: '高风险', className: 'high' }
  if (v.includes('low') || v.includes('低')) return { text: '低风险', className: 'low' }
  if ((row.totalScore || 0) >= 85) return { text: '高风险', className: 'high' }
  if ((row.totalScore || 0) <= 55) return { text: '低风险', className: 'low' }
  return { text: '中风险', className: 'mid' }
}

const withinDays = (dateText, days) => {
  if (!dateText) return false
  const t = new Date(dateText)
  if (Number.isNaN(t.getTime())) return false
  const diff = (Date.now() - t.getTime()) / (24 * 3600 * 1000)
  return diff <= days
}

const applyFilter = () => {
  filteredList.value = list.value.filter((row) => {
    const keyOk = !keyword.value || `${row.name || ''}${row.studentNo || ''}${row.studentId || ''}`.includes(keyword.value.trim())
    const risk = riskInfo(row).className
    const riskOk = riskFilter.value === 'all' || risk === riskFilter.value
    const timeOk = timeFilter.value === 'all' || withinDays(row.lastConsultTime || row.lastTime, Number(timeFilter.value))
    return keyOk && riskOk && timeOk
  })
}

const rowClassName = ({ row }) => (riskInfo(row).className === 'high' ? 'row-risk-high' : '')

const avatarText = (name) => (name ? name.slice(-1) : '学')

const editNote = (row) => {
  currentStudentId.value = row.studentId
  noteText.value = row.note || ''
  showNote.value = true
}

const saveNote = async () => {
  await axios.post(`/api/counselor/students/${currentStudentId.value}/note`, { note: noteText.value })
  window.$message?.success('备注已保存')
  showNote.value = false
  load()
}

const saveInlineNote = async (row) => {
  if (!row.studentId) return
  await axios.post(`/api/counselor/students/${row.studentId}/note`, { note: row.note || '' })
  editingId.value = 0
  window.$message?.success('已保存')
}

const openDrawer = (row) => {
  currentStudent.value = row
  showDrawer.value = true
}

const bulkTag = (mode) => {
  const text = mode === 'follow' ? '已批量标记为跟进' : '已批量标记为复评'
  window.$message?.success(text)
}

const viewReports = (row) => {
  router.push({ path: '/counselor/reports', query: { studentId: row.studentId } })
}

onMounted(load)
</script>

<style scoped>
.page { max-width:1080px; margin:0 auto }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px; gap: 10px; flex-wrap: wrap; }
.ops { display:flex; gap: 8px; flex-wrap: wrap; }
.batch-bar {
  margin-bottom: 10px;
  padding: 8px 12px;
  border: 1px solid #ddd6fe;
  background: #f5f3ff;
  border-radius: 10px;
  display:flex;
  align-items:center;
  gap: 8px;
}
.student-cell { display:flex; align-items:center; gap:10px; }
.avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #ede9fe;
  color: #6d28d9;
  display:flex;
  align-items:center;
  justify-content:center;
  font-weight: 700;
}
.name { color: var(--color-text-primary); font-weight: 600; }
.sub { color: var(--color-text-tertiary); font-size: 12px; }
.risk-dot {
  display:inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}
.risk-dot.low { background: #10b981; }
.risk-dot.mid { background: #f59e0b; }
.risk-dot.high { background: #ef4444; }
.note { color:#4b5563; cursor: text; }

:deep(.row-risk-high > td) {
  background: #fef2f2 !important;
}

:deep(.el-table__row:hover > td) {
  background: var(--color-bg-tertiary) !important;
}
</style>
