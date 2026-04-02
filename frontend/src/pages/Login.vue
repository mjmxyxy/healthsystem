<template>
  <div class="login-page">
    <div class="auth-intro">
      <h1>欢迎使用安心咨询</h1>
      <p>在这里开始你的心理测评、预约咨询和成长记录。请先登录进入个人空间。</p>
    </div>

    <el-card class="box-card">
      <div slot="header">登录</div>
      <el-form :model="form">
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="账号"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input type="password" v-model="form.password" placeholder="密码"></el-input>
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="form.role" placeholder="请选择身份" style="width: 100%">
            <el-option label="学生" value="student"></el-option>
            <el-option label="咨询师" value="counselor"></el-option>
            <el-option label="管理员" value="admin"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="button" @click="onLogin">登录</el-button>
          <el-button native-type="button" @click="goRegister" style="margin-left:8px">注册</el-button>
          <el-button native-type="button" @click="goReset" style="margin-left:8px">忘记密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const form = reactive({ account: '', password: '', role: 'student' })

const onLogin = async () => {
  try {
    const res = await axios.post('/api/auth/login', form)
    // support wrapped responses: { code,message,data:{ token,... } }
    const wrapper = res.data || {}
    const data = wrapper.data || wrapper
    if (data && data.token) {
      localStorage.setItem('token', data.token)
      localStorage.setItem('jwt_token', data.token)
      localStorage.setItem('role', data.role || form.role)
      localStorage.setItem('account', form.account)
      if (data.userId) localStorage.setItem('user_id', data.userId)
      axios.defaults.headers.common['Authorization'] = 'Bearer ' + data.token
      ElMessage.success('登录成功')
      const r = data.role || form.role
      if (r === 'student') router.push('/student/home')
      else if (r === 'counselor') router.push('/counselor/home')
      else router.push('/admin/home')
      return
    }
    ElMessage.error('登录失败：服务器未返回 token')
  } catch (err) {
    console.error(err)
    const resp = err?.response?.data
    const message = resp?.msg || resp?.message || resp?.data?.error || resp?.error || err.message
    if (err?.response?.status === 401) {
      ElMessage.error(message || '账号、密码或身份不匹配，请检查后重试')
      return
    }
    ElMessage.error(message || '登录失败，请检查后端是否启动')
  }
}

const goRegister = () => router.push('/register')
const goReset = () => router.push('/reset')
</script>

<style scoped>
.login-page {
  display:flex;
  flex-direction:column;
  justify-content:center;
  align-items:center;
  min-height:100vh;
  padding: 24px 16px;
  gap: 16px;
  background-image:
    linear-gradient(135deg, rgba(233, 245, 246, 0.42), rgba(244, 249, 255, 0.48)),
    url('/images/brance.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}
.auth-intro {
  max-width: 460px;
  text-align: center;
  color: #0f2742;
}
.auth-intro h1 {
  margin: 0 0 8px;
  font-size: 40px;
  font-weight: 800;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 12px rgba(255, 255, 255, 0.75);
}
.auth-intro p {
  margin: 0;
  color: #294661;
  line-height: 1.8;
  font-size: 16px;
  font-weight: 500;
}
.box-card { width:400px }

:deep(.el-card) {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
}

@media (max-width: 768px) {
  .auth-intro h1 {
    font-size: 32px;
  }

  .auth-intro p {
    font-size: 15px;
  }
}
</style>
