<template>
  <div class="register-page">
    <div class="auth-intro">
      <h1>欢迎加入安心咨询</h1>
      <p>创建账号后，你可以完成测评、预约咨询，并持续查看个人心理成长记录。</p>
    </div>

    <el-card class="box-card">
      <div slot="header">注册</div>
      <el-form :model="form">
        <el-form-item>
          <el-input v-model="form.account" placeholder="账号"></el-input>
        </el-form-item>
        <el-form-item>
          <el-input type="password" v-model="form.password" placeholder="密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.name" placeholder="姓名"></el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.studentId" placeholder="学号（学生）"></el-input>
        </el-form-item>
        <el-form-item>
          <el-select v-model="form.role" placeholder="请选择身份" style="width: 100%">
            <el-option label="学生" value="student"></el-option>
            <el-option label="咨询师" value="counselor"></el-option>
            <el-option label="管理员" value="admin"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onRegister">注册</el-button>
          <el-button @click="goLogin">返回登录</el-button>
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
const form = reactive({ account: '', password: '', name: '', studentId: '', role: 'student' })

const onRegister = async () => {
  try {
    const res = await axios.post('/api/auth/register', form)
    ElMessage.success('注册成功，前往登录')
    router.push('/')
  } catch (err) {
    console.error(err)
    ElMessage.error(err?.response?.data?.error || '注册失败')
  }
}

const goLogin = () => router.push('/')
</script>

<style scoped>
.register-page {
  display:flex;
  flex-direction:column;
  justify-content:center;
  align-items:center;
  min-height:100vh;
  padding: 24px 16px;
  gap: 16px;
  background-image:
    linear-gradient(135deg, rgba(235, 245, 239, 0.42), rgba(242, 248, 255, 0.48)),
    url('/images/manyhands.jpg');
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
.box-card { width:420px }

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
