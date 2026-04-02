<template>
  <div class="counselor-shell">
    <header class="topbar">
      <div class="brand" @click="goDashboard">
        <div class="logo">咨</div>
        <div>
          <div class="title">安心咨询</div>
          <div class="subtitle">Counselor Portal</div>
        </div>
      </div>

      <div class="top-actions">
        <el-button class="nav-btn" @click="goDashboard">工作台</el-button>
        <div class="user-meta">咨询师：{{ userLabel }}</div>
        <el-button class="nav-btn" @click="logout">退出登录</el-button>
      </div>
    </header>

    <div class="subnav">
      <el-button text @click="goAppointments">预约管理</el-button>
      <el-button text @click="goStudents">学生管理</el-button>
      <el-button text @click="goReports">测评报告</el-button>
      <el-button text @click="goArticles">科普文章</el-button>
      <el-button text @click="goProfile">个人中心</el-button>
    </div>

    <main class="content">
      <router-view />
    </main>

    <footer class="counselor-shell-footer">© 2026 安心咨询 · Counselor Workspace</footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()

const userLabel = computed(() => {
  const account = localStorage.getItem('account')
  const userId = localStorage.getItem('user_id')
  return account || (userId ? `ID ${userId}` : '咨询师')
})

const goDashboard = () => router.push('/counselor/dashboard')
const goAppointments = () => router.push('/counselor/appointments')
const goStudents = () => router.push('/counselor/students')
const goReports = () => router.push('/counselor/reports')
const goArticles = () => router.push('/counselor/articles')
const goProfile = () => router.push('/counselor/profile')

const logout = async () => {
  try {
    await ElMessageBox.confirm('确认退出登录吗？', '退出登录', { type: 'warning' })
    localStorage.removeItem('token')
    localStorage.removeItem('jwt_token')
    localStorage.removeItem('role')
    localStorage.removeItem('user_id')
    localStorage.removeItem('account')
    router.push('/')
  } catch (_) {}
}
</script>

<style scoped>
.counselor-shell { min-height: 100vh; background: #f9fafb; font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif; }
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
.user-meta { color:#374151; font-size: 13px; padding: 6px 10px; border-radius: 8px; border: 1px solid #d1d5db; background: #f9fafb; }
.nav-btn { border: 1px solid #d1d5db; background: #ffffff; color:#1f2937; border-radius: 8px; }
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
.subnav :deep(.el-button) { color: #374151; }
.content { padding: 24px 16px 88px; }
.counselor-shell-footer { margin-top: 20px; text-align: center; color: #6b7280; font-size: 12px; padding-bottom: 18px; }
@media (max-width: 960px) {
  .topbar { flex-direction:column; align-items:flex-start; gap:10px }
  .top-actions { width: 100%; justify-content: space-between }
  .subnav { top: 102px }
}
</style>
