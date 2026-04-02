<template>
  <div class="student-shell" :class="{ 'home-bg': isStudentHome }">
    <header class="topbar">
      <div class="brand" @click="goHome">
        <div class="logo">安</div>
        <div>
          <div class="title">安心咨询</div>
          <div class="subtitle">Student Portal</div>
        </div>
      </div>

      <nav class="main-nav">
        <el-button text @click="goScales">心理测评</el-button>
        <el-button text @click="goAssessments">测评历史</el-button>
        <el-button text @click="goCounselors">咨询师预约</el-button>
        <el-button text @click="goAppointments">我的预约</el-button>
        <el-button text @click="goArticles">心理科普</el-button>
        <el-button text @click="goProfile">个人中心</el-button>
      </nav>

      <div class="top-actions">
        <el-button class="nav-btn" @click="goHome">返回首页</el-button>
        <div class="user-meta">用户：{{ userLabel }}</div>
        <el-button class="nav-btn" @click="logout">退出登录</el-button>
      </div>
    </header>

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
import { useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import FloatingAI from './FloatingAI.vue'
import AiChatPanel from './AiChatPanel.vue'

const router = useRouter()
const route = useRoute()
const showAi = ref(false)
const isStudentHome = computed(() => route.path === '/student/home')

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
.student-shell {
  min-height: 100vh;
  background: var(--app-background);
  font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.student-shell.home-bg {
  background-image:
    linear-gradient(135deg, rgba(233, 245, 246, 0.38), rgba(244, 249, 255, 0.42)),
    url('/images/xiaojing.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 25;
  display:flex;
  justify-content:space-between;
  align-items:center;
  gap:12px;
  flex-wrap: wrap;
  padding: 12px 20px;
  background: var(--color-bg-primary);
  border-bottom: 1px solid var(--color-border);
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06);
}
.brand { display:flex; align-items:center; gap:12px; cursor: pointer }
.logo {
  width:42px; height:42px; border-radius:12px; display:flex; align-items:center; justify-content:center;
  background: linear-gradient(135deg, var(--color-student) 0%, var(--color-primary) 100%);
  color:#fff; font-weight:700; font-size:20px;
}
.title { font-size:16px; font-weight:700; color: var(--color-text-primary) }
.subtitle { font-size:12px; color: var(--color-text-secondary) }

.main-nav {
  display:flex;
  flex-wrap:wrap;
  align-items:center;
  gap: 4px;
}

.main-nav :deep(.el-button) {
  color: var(--color-text-secondary);
}

.top-actions { display:flex; align-items:center; gap:10px; margin-left: auto; }
.user-meta {
  color: var(--color-text-secondary);
  font-size: 13px;
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-secondary);
}

.nav-btn {
  border: 1px solid var(--color-border);
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
}

.content { padding: 24px 28px 88px; }

:deep(.ai-dialog .el-dialog__body) { padding-top: 0; }
@media (max-width: 960px) {
  .topbar { align-items:flex-start; gap: 10px }
  .main-nav { width: 100%; order: 3; }
  .top-actions { width:100%; justify-content:space-between }
  .content { padding: 16px 12px 72px; }
}
</style>
