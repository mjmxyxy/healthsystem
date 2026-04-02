<template>
  <div class="page">
    <div class="cards">
      <el-card class="stat-card">
        <div class="label">学生总数</div>
        <div class="value">{{ stats.studentCount }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="label">咨询师总数</div>
        <div class="value">{{ stats.counselorCount }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="label">测评报告数</div>
        <div class="value">{{ stats.assessmentCount }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="label">预约订单数</div>
        <div class="value">{{ stats.appointmentCount }}</div>
      </el-card>
      <el-card class="stat-card highlight">
        <div class="label">待审核文章</div>
        <div class="value">{{ stats.pendingArticleCount }}</div>
      </el-card>
    </div>

    <el-card v-loading="loading" style="margin-top: 14px">
      <div class="head">
        <h2>快捷入口</h2>
      </div>
      <div class="quick-actions">
        <el-button @click="go('/admin/students')">学生管理</el-button>
        <el-button @click="go('/admin/counselors')">咨询师管理</el-button>
        <el-button @click="go('/admin/scales')">量表题目管理</el-button>
        <el-button @click="go('/admin/appointments')">预约订单管理</el-button>
        <el-button type="warning" @click="go('/admin/articles')">文章审核</el-button>
        <el-button type="primary" @click="go('/admin/profile')">个人中心</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const stats = ref({
  studentCount: 0,
  counselorCount: 0,
  assessmentCount: 0,
  appointmentCount: 0,
  pendingArticleCount: 0
})

const go = (path) => router.push(path)

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/dashboard')
    const body = res?.data || {}
    stats.value = body.data || body
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '控制台数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page {
  max-width: 1180px;
  margin: 0 auto;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.stat-card .label {
  color: #6b7280;
  font-size: 13px;
}

.stat-card .value {
  color: #111827;
  font-size: 30px;
  font-weight: 700;
  margin-top: 8px;
}

.stat-card.highlight {
  border: 1px solid #facc15;
}

.head {
  margin-bottom: 12px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
