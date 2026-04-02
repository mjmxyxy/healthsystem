<template>
  <div class="reset-page">
    <el-card class="box-card">
      <div slot="header">重置密码</div>
      <el-form :model="form">
        <el-form-item>
          <el-input v-model="form.account" placeholder="账号"></el-input>
        </el-form-item>
        <el-form-item>
          <el-input type="password" v-model="form.newPassword" placeholder="新密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onReset">提交重置</el-button>
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
const form = reactive({ account: '', newPassword: '' })

const onReset = async () => {
  try {
    const res = await axios.post('/api/auth/reset', form)
    ElMessage.success('密码重置成功，前往登录')
    router.push('/')
  } catch (err) {
    console.error(err)
    ElMessage.error(err?.response?.data?.error || '重置失败')
  }
}

const goLogin = () => router.push('/')
</script>

<style scoped>
.reset-page { display:flex; justify-content:center; align-items:center; height:100vh }
.box-card { width:420px }
</style>
