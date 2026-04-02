<template>
  <div class="page">
    <el-card v-loading="loading">
      <div class="head">
        <h2>学生管理</h2>
        <el-button @click="load">刷新</el-button>
      </div>
      <el-table :data="list">
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="account" label="账号" />
        <el-table-column prop="studentNo" label="学号" />
        <el-table-column prop="gender" label="性别" width="90" />
        <el-table-column prop="reportCount" label="测评数" width="90" />
        <el-table-column label="咨询备注">
          <template #default="{ row }">
            <div class="note">{{ row.note || '暂无备注' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" @click="editNote(row)">编辑备注</el-button>
            <el-button size="small" type="primary" @click="viewReports(row)">查看报告</el-button>
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
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const showNote = ref(false)
const noteText = ref('')
const currentStudentId = ref(0)

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/counselor/students')
    const body = res.data || {}
    list.value = body.data || body
  } finally {
    loading.value = false
  }
}

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

const viewReports = (row) => {
  router.push({ path: '/counselor/reports', query: { studentId: row.studentId } })
}

onMounted(load)
</script>

<style scoped>
.page { max-width:1080px; margin:0 auto }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px }
.note { color:#4b5563 }
</style>
