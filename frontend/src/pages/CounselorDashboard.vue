<template>
  <div class="page">
    <div class="cards">
      <el-card class="stat-card">
        <div class="label">待处理预约</div>
        <div class="value">{{ stats.pendingAppointments }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="label">今日已确认</div>
        <div class="value">{{ stats.todayConfirmed }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="label">后续咨询</div>
        <div class="value">{{ stats.upcomingConsultations }}</div>
      </el-card>
    </div>

    <el-card v-loading="loading" style="margin-top:16px">
      <div class="head">
        <h2>近期预约</h2>
        <el-button @click="goAppointments">进入预约管理</el-button>
      </div>
      <el-table :data="stats.recentAppointments || []">
        <el-table-column prop="studentName" label="学生" />
        <el-table-column prop="startTime" label="预约时间" />
        <el-table-column prop="status" label="状态" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openChat(row)">进入聊天</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const stats = ref({ pendingAppointments: 0, todayConfirmed: 0, upcomingConsultations: 0, recentAppointments: [] })

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/counselor/dashboard')
    const body = res.data || {}
    stats.value = body.data || body
  } finally {
    loading.value = false
  }
}

const goAppointments = () => router.push('/counselor/appointments')
const openChat = (row) => {
  const studentId = row.studentId
  if (!studentId) return
  router.push(`/counselor/chat/${studentId}`)
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1080px; margin: 0 auto; }
.cards { display:grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px; }
.stat-card .label { color:#6b7280; font-size:13px }
.stat-card .value { color:#111827; font-size:32px; font-weight:700; margin-top:8px }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px }
</style>
