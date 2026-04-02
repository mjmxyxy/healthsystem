<template>
  <div class="page">
    <el-card>
      <h2>个人中心</h2>
      <el-form :model="profile" label-width="90px" style="max-width:640px">
        <el-form-item label="账号">
          <el-input v-model="profile.account" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="profile.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="profile.gender" clearable style="width:100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="profile.avatar" placeholder="https://..." />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
          <el-button @click="goHistory">测评历史</el-button>
          <el-button @click="goAppointments">预约记录</el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <el-form :model="pwd" label-width="90px" style="max-width:640px">
        <el-form-item label="原密码">
          <el-input v-model="pwd.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwd.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="warning" :loading="changingPwd" @click="changePwd">修改密码</el-button>
          <el-button type="danger" @click="logout">退出登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userId = Number(localStorage.getItem('user_id') || 0)
const profile = ref({ account: '', nickname: '', gender: '', avatar: '' })
const pwd = ref({ oldPassword: '', newPassword: '' })
const saving = ref(false)
const changingPwd = ref(false)

const load = async () => {
  if (!userId) {
    router.push('/')
    return
  }
  const res = await axios.get('/api/student/profile', { params: { userId } })
  const body = res.data || {}
  const data = body.data || body
  profile.value = {
    account: data.account || '',
    nickname: data.nickname || '',
    gender: data.gender || '',
    avatar: data.avatar || ''
  }
}

const saveProfile = async () => {
  if (!profile.value.nickname || profile.value.nickname.trim().length < 2) {
    window.$message?.error('昵称至少 2 个字符')
    return
  }
  if (profile.value.avatar && !/^https?:\/\//.test(profile.value.avatar)) {
    window.$message?.error('头像 URL 需以 http:// 或 https:// 开头')
    return
  }
  saving.value = true
  try {
    await axios.put('/api/student/profile', {
      userId,
      nickname: profile.value.nickname,
      gender: profile.value.gender,
      avatar: profile.value.avatar
    })
    window.$message?.success('资料已更新')
  } finally {
    saving.value = false
  }
}

const changePwd = async () => {
  if (!pwd.value.oldPassword || !pwd.value.newPassword) {
    window.$message?.error('请填写原密码和新密码')
    return
  }
  if (pwd.value.newPassword.length < 6) {
    window.$message?.error('新密码至少 6 位')
    return
  }
  changingPwd.value = true
  try {
    await axios.put('/api/student/password', { userId, ...pwd.value })
    window.$message?.success('密码已修改，请重新登录')
    await logout()
  } finally {
    changingPwd.value = false
  }
}

const logout = async () => {
  try {
    await ElMessageBox.confirm('确认退出登录吗？', '退出登录', { type: 'warning' })
    localStorage.removeItem('token')
    localStorage.removeItem('jwt_token')
    localStorage.removeItem('role')
    localStorage.removeItem('user_id')
    router.push('/')
  } catch (_) {}
}

const goHistory = () => router.push('/student/assessments')
const goAppointments = () => router.push('/student/appointments')

onMounted(load)
</script>

<style scoped>
.page { max-width:960px; margin:24px auto; padding:0 16px }
</style>
