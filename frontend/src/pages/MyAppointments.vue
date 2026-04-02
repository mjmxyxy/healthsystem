<template>
  <div class="page">
    <el-card v-loading="loading">
      <div class="head">
        <h2>我的预约</h2>
        <el-button @click="goHome">返回首页</el-button>
      </div>
      <el-table :data="list">
        <el-table-column prop="counselorName" label="咨询师"></el-table-column>
        <el-table-column prop="startTime" label="开始"></el-table-column>
        <el-table-column prop="duration" label="时长(分)"></el-table-column>
        <el-table-column label="状态">
          <template #default="{row}">
            <el-tag :type="tagType(row)">{{ statusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{row}">
            <div class="table-actions cols-2">
              <el-button size="small" type="danger" :disabled="!canCancel(row)" @click="cancel(row)">取消</el-button>
              <el-button size="small" @click.stop="enterChat(row)">进入聊天</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const router = useRouter()

const load = async () => {
  loading.value = true
  try {
    const userId = Number(localStorage.getItem('user_id') || 0)
    if (!userId) { window.$message?.error('请先登录'); router.push('/'); return }
    const res = await axios.get(`/api/appointments?studentId=${userId}`)
    const body = res.data || {}
    const rows = body.data || body
    list.value = Array.isArray(rows) ? rows : []
  } finally {
    loading.value = false
  }
}

const getStatusCode = (row) => Number(row.statusCode ?? row.status_code ?? 0)
const statusText = (row) => {
  const code = getStatusCode(row)
  return row.status || row.statusText || (code === 0 ? '待确认' : code === 1 ? '已确认' : code === 2 ? '已完成' : '已取消')
}
const tagType = (row) => {
  const code = getStatusCode(row)
  if (code === 0) return 'warning'
  if (code === 1) return 'success'
  if (code === 2) return 'info'
  return 'info'
}
const canCancel = (row) => getStatusCode(row) === 0
const canChat = (row) => getStatusCode(row) === 1

const cancel = async (row) => {
  if (!canCancel(row)) return
  try {
    await ElMessageBox.confirm('确认取消该预约吗？', '取消预约', { type: 'warning' })
  } catch (_) {
    return
  }
  const userId = Number(localStorage.getItem('user_id') || 0)
  await axios.delete(`/api/appointments/${row.id}?studentId=${userId}`)
  window.$message?.success('已取消')
  load()
}

const enterChat = (row) => {
  const rawId = row.counselorId ?? row.counselor_id ?? row.counselorName ?? row.counselor_name
  const counselorId = Number(rawId)
  if (!counselorId || Number.isNaN(counselorId)) {
    window.$message?.error('咨询师ID缺失，无法进入聊天')
    return
  }
  if (!canChat(row)) {
    window.$message?.info('当前预约未确认，已进入聊天页面')
  }
  const target = `/student/chat/${counselorId}`
  router.push(target).catch(() => {
    window.location.href = target
  })
}

const goHome = () => router.push('/student/home')

onMounted(load)
</script>

<style scoped>
.page { max-width:900px; margin:24px auto }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px }
</style>
