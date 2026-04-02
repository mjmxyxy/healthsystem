<template>
  <div class="student-shell">
    <header class="topbar">
      <div class="brand" @click="goHome">
        <div class="logo">安</div>
        <div>
          <div class="title">安心咨询</div>
          <div class="subtitle">Student Portal</div>
        </div>
      </div>

      <div class="top-actions">
        <el-button class="nav-btn" @click="goHome">返回首页</el-button>
        <div class="user-meta">用户：{{ userLabel }}</div>
        <el-button class="nav-btn" @click="logout">退出登录</el-button>
      </div>
    </header>

    <div class="subnav">
      <el-button text @click="goScales">心理测评</el-button>
      <el-button text @click="goAssessments">测评历史</el-button>
      <el-button text @click="goCounselors">咨询师预约</el-button>
      <el-button text @click="goAppointments">我的预约</el-button>
      <el-button text @click="goArticles">心理科普</el-button>
      <el-button text @click="goProfile">个人中心</el-button>
    </div>

    <main class="content">
      <router-view />
    </main>

    <FloatingAI @open="showAi = true" />

    <el-dialog v-model="showAi" width="780px" class="ai-dialog" destroy-on-close>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center; width:100%">
          <span>AI 心理助手</span>
          <el-button text @click="showAi = false">关闭</el-button>
        </div>
      </template>
      <AiChatPanel />
    </el-dialog>

    <footer class="student-shell-footer">
      © 2026 安心咨询 · Campus Mental Wellness Platform
    </footer>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import FloatingAI from './FloatingAI.vue'
import AiChatPanel from './AiChatPanel.vue'

const router = useRouter()
const showAi = ref(false)

const goHome = () => router.push('/student/home')
const goScales = () => router.push('/student/scales')
const goAssessments = () => router.push('/student/assessments')
const goCounselors = () => router.push('/student/counselors')
const goAppointments = () => router.push('/student/appointments')
const goArticles = () => router.push('/student/articles')
const goProfile = () => router.push('/student/profile')

const userLabel = computed(() => {
  const account = localStorage.getItem('account')
  const userId = localStorage.getItem('user_id')
  return account || (userId ? `ID ${userId}` : '学生')
})

const logout = async () => {
  try {
    await ElMessageBox.confirm('确认退出登录吗？', '退出登录', { type: 'warning' })
    localStorage.removeItem('token')
    localStorage.removeItem('jwt_token')
    localStorage.removeItem('role')
    localStorage.removeItem('user_id')
    ElMessage.success('已退出登录')
    router.push('/')
  } catch (_) {}
}
</script>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 25;
  display:flex;
  justify-content:space-between;
  align-items:center;
  gap:16px;
  padding: 12px 20px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06);
}
.brand { display:flex; align-items:center; gap:12px; cursor: pointer }
.logo {
  width:42px; height:42px; border-radius:12px; display:flex; align-items:center; justify-content:center;
  background: linear-gradient(135deg, #3B82F6 0%, #2563eb 100%);
  color:#fff; font-weight:700; font-size:20px;
}
.title { font-size:16px; font-weight:700; color:#111827 }
.subtitle { font-size:12px; color:#6b7280 }

.top-actions { display:flex; align-items:center; gap:10px }
.user-meta {
  color:#374151;
  font-size: 13px;
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background: #f9fafb;
}

.nav-btn {
  border: 1px solid #d1d5db;
  background: #ffffff;
  color:#1f2937;
}

.subnav {
  position: sticky;
  top: 67px;
  z-index: 20;
  display:flex;
  flex-wrap:wrap;
  gap: 8px;
  padding: 10px 16px;
  background: #f3f4f6;
  border-bottom: 1px solid #e5e7eb;
}

.subnav :deep(.el-button) {
  color: #374151;
}

:deep(.ai-dialog .el-dialog__body) { padding-top: 0; }
@media (max-width: 960px) {
  .topbar { flex-direction:column; align-items:flex-start; gap: 10px }
  .subnav { top: 102px }
  .top-actions { width:100%; justify-content:space-between }
}
</style>
