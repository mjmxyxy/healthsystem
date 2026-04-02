<template>
  <div class="page">
    <el-card v-loading="loading">
      <div class="head">
        <h2>咨询师详情</h2>
        <div>
          <el-button @click="backList">返回咨询师列表</el-button>
          <el-button type="primary" @click="showBook = true">立即预约</el-button>
        </div>
      </div>
      <p><strong>姓名：</strong>{{ info.name }}</p>
      <p><strong>称号：</strong>{{ info.title }}</p>
      <p><strong>擅长方向：</strong>{{ info.specialty }}</p>
      <p>
        <strong>简介：</strong>
        {{ briefBio }}
        <el-button text @click="toggleBio">{{ showFullBio ? '收起' : '展开' }}</el-button>
      </p>
      <p><strong>参考费用：</strong>{{ info.price }} 元 / 次</p>
      <p><strong>评分：</strong>{{ info.rating }}</p>

      <el-dialog v-model="showBook" width="520px" title="预约咨询师">
        <el-form :model="form" label-width="90px">
          <el-form-item label="开始时间">
            <el-select v-model="form.startTime" placeholder="请选择可预约时间" style="width: 100%">
            <el-option v-for="slot in info.availableSlots || []" :key="slot" :label="slot" :value="slot" />
            </el-select>
          </el-form-item>
          <el-form-item label="时长(分钟)">
            <el-input-number v-model="form.duration" :min="30" :max="120" :step="30" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.note" type="textarea" :rows="2" placeholder="可填写咨询诉求（选填）" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showBook = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submit">提交预约</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)
const info = ref({})
const form = ref({ startTime: '', duration: 30, note: '' })
const loading = ref(false)
const submitting = ref(false)
const showBook = ref(false)
const showFullBio = ref(false)

const briefBio = computed(() => {
  const bio = String(info.value.bio || '')
  if (showFullBio.value || bio.length <= 80) return bio
  return `${bio.slice(0, 80)}...`
})

const toggleBio = () => {
  showFullBio.value = !showFullBio.value
}

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get(`/api/counselors/${id}`)
    const body = res.data || {}
    info.value = body.data || body
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  const userId = Number(localStorage.getItem('user_id') || 0)
  if (!userId) { window.$message?.error('请先登录'); router.push('/'); return }
  if (!form.value.startTime) {
    window.$message?.error('请先选择可预约时间')
    return
  }
  const payload = {
    studentId: userId,
    counselorId: id,
    startTime: form.value.startTime,
    duration: form.value.duration,
    note: form.value.note
  }
  submitting.value = true
  try {
    await axios.post('/api/appointments', payload)
    window.$message?.success('预约提交成功')
    showBook.value = false
    router.push('/student/appointments')
  } finally {
    submitting.value = false
  }
}

const backList = () => router.push('/student/counselors')

onMounted(load)
</script>

<style scoped>
.page { max-width:900px; margin:24px auto }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px }
</style>
