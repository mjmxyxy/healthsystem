<template>
  <div class="register-page">
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
.register-page { display:flex; justify-content:center; align-items:center; height:100vh }
.box-card { width:420px }
</style>
