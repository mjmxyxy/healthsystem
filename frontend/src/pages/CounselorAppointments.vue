<template>
  <div class="page">
    <el-card v-loading="loading">
      <div class="head">
        <h2>预约管理</h2>
        <el-button @click="load">刷新</el-button>
      </div>
      <el-table :data="list">
        <el-table-column prop="studentName" label="学生" />
        <el-table-column prop="startTime" label="开始" />
        <el-table-column prop="duration" label="时长(分)" width="100" />
        <el-table-column prop="note" label="备注" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.statusCode === 0 ? 'warning' : row.statusCode === 1 ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320">
          <template #default="{ row }">
            <div class="table-actions cols-4">
              <el-button size="small" type="primary" :disabled="row.statusCode !== 0" @click="approve(row)">同意</el-button>
              <el-button size="small" type="danger" :disabled="row.statusCode !== 0" @click="reject(row)">拒绝</el-button>
              <el-button size="small" :disabled="![0,1].includes(row.statusCode)" @click="cancel(row)">取消</el-button>
              <el-button size="small" :disabled="row.statusCode !== 1" @click="enterChat(row)">聊天</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const list = ref([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/counselor/appointments')
    const body = res.data || {}
    list.value = body.data || body
  } finally {
    loading.value = false
  }
}

const approve = async (row) => {
  await axios.post(`/api/counselor/appointments/${row.id}/approve`)
  window.$message?.success('已同意预约')
  load()
}

const reject = async (row) => {
  const reason = await ElMessageBox.prompt('请输入拒绝原因（可选）', '拒绝预约', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: ''
  }).catch(() => null)
  if (!reason) return
  await axios.post(`/api/counselor/appointments/${row.id}/reject`, { reason: reason.value || '' })
  window.$message?.success('已拒绝预约')
  load()
}

const cancel = async (row) => {
  await ElMessageBox.confirm('确认取消该预约吗？', '提示', { type: 'warning' })
  await axios.post(`/api/counselor/appointments/${row.id}/cancel`)
  window.$message?.success('已取消预约')
  load()
}

const enterChat = (row) => {
  if (row.statusCode !== 1) {
    window.$message?.warning('仅已同意预约可进入聊天')
    return
  }
  router.push(`/counselor/chat/${row.studentId}`)
}

onMounted(load)
</script>

<style scoped>
.page { max-width:1080px; margin:0 auto }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px }
</style>
