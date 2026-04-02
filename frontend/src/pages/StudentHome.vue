<template>
  <div class="page">
    <el-card>
        <h2>学生首页</h2>
        <p class="sub">把今天的心理状态照顾好，从一次测评或一次倾诉开始。</p>
        <el-divider />
        <div class="cards">
          <div class="nav-card card-hover" @click="goScales">
            <h3>心理测评</h3>
            <p>选择量表并完成答题，系统生成报告</p>
          </div>
          <div class="nav-card card-hover" @click="goHistory">
            <h3>测评历史</h3>
            <p>查看过往分数趋势与建议</p>
          </div>
          <div class="nav-card card-hover" @click="goCounselors">
            <h3>咨询师预约</h3>
            <p>筛选咨询师并提交预约</p>
          </div>
          <div class="nav-card card-hover" @click="goAppointments">
            <h3>我的预约</h3>
            <p>查看状态、取消预约、进入聊天</p>
          </div>
          <div class="nav-card card-hover" @click="goArticles">
            <h3>心理科普</h3>
            <p>阅读咨询师发布的心理健康科普文章</p>
          </div>
          <div class="nav-card card-hover" @click="goProfile">
            <h3>个人中心</h3>
            <p>修改资料与密码，安全退出登录</p>
          </div>
        </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const goScales = () => router.push('/student/scales')
const goHistory = () => router.push('/student/assessments')
const goCounselors = () => router.push('/student/counselors')
const goAppointments = () => router.push('/student/appointments')
const goArticles = () => router.push('/student/articles')
const goProfile = () => router.push('/student/profile')
</script>

<style scoped>
.page {
  max-width: 960px;
  margin: 40px auto;
  padding: 0 16px;
}
.sub { color: #666 }
.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}
.nav-card {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 18px;
  cursor: pointer;
  background: #ffffff;
  transition: all .2s ease;
}
.nav-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(15, 23, 42, .12);
}
.nav-card h3 { margin: 0 0 8px }
.nav-card p { margin: 0; color: #666 }
</style>
