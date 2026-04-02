<template>
  <div class="page">
    <el-card v-loading="loading">
      <h2>个人中心</h2>
      <el-form :model="profile" label-width="90px" style="max-width:640px">
        <el-form-item label="账号">
          <el-input v-model="profile.account" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="profile.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="profile.gender" clearable style="width:100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile">保存资料</el-button>
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
          <el-button type="warning" @click="changePwd">修改密码</el-button>
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
const loading = ref(false)
const profile = ref({ account: '', name: '', gender: '' })
const pwd = ref({ oldPassword: '', newPassword: '' })

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/counselor/profile')
    const body = res.data || {}
    const data = body.data || body
    profile.value = { account: data.account || '', name: data.name || '', gender: data.gender || '' }
  } finally {
    loading.value = false
  }
}

const saveProfile = async () => {
  await axios.put('/api/counselor/profile', { name: profile.value.name, gender: profile.value.gender })
  window.$message?.success('资料已更新')
}

const changePwd = async () => {
  if (!pwd.value.oldPassword || !pwd.value.newPassword) {
    window.$message?.error('请输入原密码和新密码')
    return
  }
  await axios.put('/api/counselor/password', pwd.value)
  window.$message?.success('密码已修改，请重新登录')
  await logout()
}

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

onMounted(load)
</script>

<style scoped>
.page { max-width:1080px; margin:0 auto }
</style>
