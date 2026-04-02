<template>
  <div class="student-home">
    <div class="container">
      <section class="banner">
        <div class="banner-content">
          <span class="banner-tag">团体支持 · 温柔陪伴</span>
          <h1 class="banner-title">在这里，你会被看见，被理解。</h1>
          <p class="banner-desc">如果今天有一点沉重，就从一次测评、一次预约，或者一句话开始。</p>
        </div>
        <div class="banner-img"></div>
      </section>

      <section class="welcome-section">
        <h2 class="welcome-title">欢迎回来，{{ username }}</h2>
        <p class="welcome-desc">把今天的心理状态照顾好，从一次测评或一次倾诉开始。</p>
      </section>

      <section class="mood-card glass-card">
        <div class="mood-header">
          <div class="mood-icon">☀️</div>
          <span class="mood-title">今天感觉如何？</span>
        </div>
        <div class="mood-buttons">
          <el-button @click="setMood('happy')" :type="currentMood === 'happy' ? 'primary' : 'default'">开心 😊</el-button>
          <el-button @click="setMood('calm')" :type="currentMood === 'calm' ? 'primary' : 'default'">平静 😌</el-button>
          <el-button @click="setMood('anxious')" :type="currentMood === 'anxious' ? 'primary' : 'default'">焦虑 😰</el-button>
          <el-button @click="setMood('sad')" :type="currentMood === 'sad' ? 'primary' : 'default'">难过 😢</el-button>
        </div>
      </section>

      <section class="func-grid">
        <div class="func-card card-yellow glass-card">
          <div class="card-header">
            <span class="card-title">PHQ-9 抑郁筛查</span>
            <span class="card-tag tag-yellow">剩余 2 天</span>
          </div>
          <p class="card-desc">建议本周内完成</p>
          <el-button type="primary" size="small" class="card-btn" @click="goScales">立即测评</el-button>
        </div>

        <div class="func-card card-green glass-card">
          <div class="card-header">
            <span class="card-title">咨询预约提醒</span>
            <span class="card-tag tag-green">即将开始</span>
          </div>
          <p class="card-desc">周五 15:00 与李老师</p>
          <el-button type="success" size="small" class="card-btn" @click="goAppointments">进入咨询</el-button>
        </div>

        <div class="func-card card-blue glass-card">
          <div class="card-header">
            <span class="card-title">阅读科普文章</span>
            <span class="card-tag tag-blue">进行中</span>
          </div>
          <p class="card-desc">压力管理与睡眠</p>
          <el-button type="primary" size="small" class="card-btn" @click="goArticles">去阅读</el-button>
        </div>

        <div class="func-card card-pink glass-card">
          <div class="card-header">
            <span class="card-title">更新个人资料</span>
            <span class="card-tag tag-pink">待处理</span>
          </div>
          <p class="card-desc">完善联系方式</p>
          <el-button type="primary" size="small" class="card-btn" @click="goProfile">去完善</el-button>
        </div>
      </section>

      <section class="quick-section">
        <h3 class="section-title">快捷功能</h3>
        <div class="quick-grid">
          <div class="quick-card glass-card" @click="goScales">
            <div class="quick-icon bg-blue">🧠</div>
            <span class="quick-text">心理测评</span>
          </div>
          <div class="quick-card glass-card" @click="goHistory">
            <div class="quick-icon bg-green">📊</div>
            <span class="quick-text">测评历史</span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = computed(() => localStorage.getItem('account') || '同学')
const currentMood = ref('calm')

const setMood = (mood) => {
  currentMood.value = mood
}

const goScales = () => router.push('/student/scales')
const goHistory = () => router.push('/student/assessments')
const goAppointments = () => router.push('/student/appointments')
const goArticles = () => router.push('/student/articles')
const goProfile = () => router.push('/student/profile')
</script>

<style scoped>
.student-home {
  min-height: calc(100vh - 120px);
  padding-bottom: 28px;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 12px;
}

.banner {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  align-items: center;
  background: rgba(255, 255, 255, 0.72);
  border-radius: 20px;
  overflow: hidden;
  margin: 18px 0;
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
  border: 1px solid rgba(255, 255, 255, 0.42);
  backdrop-filter: blur(10px);
}

.banner-content {
  padding: 40px;
}

.banner-tag {
  background: rgba(243, 244, 246, 0.76);
  color: #4ECDC4;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.banner-title {
  font-size: 52px;
  font-weight: 800;
  color: #1f2937;
  margin: 14px 0 8px;
}

.banner-desc {
  color: #6b7280;
  font-size: 15px;
}

.banner-img {
  height: 300px;
  background-image: url('/images/brance.jpg');
  background-size: cover;
  background-position: center;
}

.welcome-section {
  margin-bottom: 18px;
}

.welcome-title {
  font-size: 36px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.welcome-desc {
  color: #6b7280;
  font-size: 14px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(8px);
}

.mood-card {
  border-radius: 16px;
  padding: 22px;
  margin-bottom: 18px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
}

.mood-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.mood-icon {
  font-size: 32px;
}

.mood-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.mood-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.mood-buttons .el-button {
  border-radius: 20px;
  padding: 6px 16px;
}

.func-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.func-card {
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
  transition: transform 0.2s, box-shadow 0.2s;
}

.func-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
}

.card-yellow {
  background: linear-gradient(135deg, rgba(255, 251, 235, 0.70) 0%, rgba(254, 243, 199, 0.72) 100%);
}

.card-green {
  background: linear-gradient(135deg, rgba(240, 253, 244, 0.70) 0%, rgba(209, 250, 229, 0.72) 100%);
}

.card-blue {
  background: linear-gradient(135deg, rgba(239, 246, 255, 0.70) 0%, rgba(224, 231, 255, 0.72) 100%);
}

.card-pink {
  background: linear-gradient(135deg, rgba(253, 242, 248, 0.70) 0%, rgba(252, 231, 243, 0.72) 100%);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 10px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.card-tag {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.tag-yellow {
  background: #fbbf24;
  color: #fff;
}

.tag-green {
  background: #10b981;
  color: #fff;
}

.tag-blue {
  background: #3b82f6;
  color: #fff;
}

.tag-pink {
  background: #ec4899;
  color: #fff;
}

.card-desc {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 12px;
}

.card-btn {
  width: 100%;
  border-radius: 10px;
}

.quick-section {
  margin-top: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 14px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.quick-card {
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
  transition: transform 0.2s;
  cursor: pointer;
}

.quick-card:hover {
  transform: translateY(-3px);
}

.quick-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 24px;
}

.bg-blue {
  background: #dbeafe;
  color: #3b82f6;
}

.bg-green {
  background: #d1fae5;
  color: #10b981;
}

.quick-text {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
}

@media (max-width: 1024px) {
  .func-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .banner {
    grid-template-columns: 1fr;
  }

  .banner-img {
    height: 220px;
  }

  .banner-title {
    font-size: 42px;
  }

  .welcome-title {
    font-size: 30px;
  }
}

@media (max-width: 768px) {
  .func-grid,
  .quick-grid {
    grid-template-columns: 1fr;
  }

  .banner-content {
    padding: 24px;
  }

  .banner-title {
    font-size: 34px;
  }

  .welcome-title {
    font-size: 26px;
  }
}
</style>
