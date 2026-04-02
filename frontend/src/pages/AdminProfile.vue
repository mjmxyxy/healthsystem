<template>
  <div class="page">
    <el-row :gutter="14">
      <el-col :xs="24" :md="12">
        <el-card>
          <h2>个人中心</h2>
          <el-form :model="profile" label-width="80px" style="margin-top: 10px">
            <el-form-item label="账号">
              <el-input v-model="profile.account" disabled />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="profile.name" />
            </el-form-item>
            <el-form-item label="性别">
              <el-select v-model="profile.gender" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
                <el-option label="未知" value="未知" />
              </el-select>
            </el-form-item>
          </el-form>
          <el-button type="primary" @click="saveProfile">保存资料</el-button>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card>
          <h2>修改密码</h2>
          <el-form :model="pwd" label-width="90px" style="margin-top: 10px">
            <el-form-item label="原密码">
              <el-input v-model="pwd.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwd.newPassword" type="password" show-password />
            </el-form-item>
          </el-form>
          <el-button type="warning" @click="changePassword">修改密码</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const profile = reactive({ id: null, account: '', name: '', gender: '未知', role: 'admin' })
const pwd = reactive({ oldPassword: '', newPassword: '' })

const parseData = (res) => {
  const body = res?.data || {}
  return body.data || body
}

const load = async () => {
  try {
    const res = await axios.get('/api/admin/profile')
    const data = parseData(res) || {}
    profile.id = data.id
    profile.account = data.account || ''
    profile.name = data.name || ''
    profile.gender = data.gender || '未知'
    profile.role = data.role || 'admin'
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '加载资料失败')
  }
}

const saveProfile = async () => {
  try {
    await axios.put('/api/admin/profile', { name: profile.name, gender: profile.gender })
    ElMessage.success('资料已更新')
    await load()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '保存失败')
  }
}

const changePassword = async () => {
  if (!pwd.oldPassword || !pwd.newPassword) {
    ElMessage.error('请填写完整密码')
    return
  }
  try {
    await axios.put('/api/admin/password', { oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    pwd.oldPassword = ''
    pwd.newPassword = ''
    localStorage.removeItem('token')
    localStorage.removeItem('jwt_token')
    window.location.href = '/'
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '密码修改失败')
  }
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1100px; margin: 0 auto; }
</style>
