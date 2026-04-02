<template>
  <div class="page">
    <el-card v-loading="loading">
      <div class="head">
        <h2>咨询师列表</h2>
        <el-button @click="goHome">返回首页</el-button>
      </div>
      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索姓名/账号" clearable @input="load" />
        <el-select v-model="gender" placeholder="性别筛选" clearable @change="load" style="width:160px">
          <el-option label="男" value="男" />
          <el-option label="女" value="女" />
        </el-select>
        <el-select v-model="specialty" placeholder="擅长领域筛选" clearable @change="load" style="width:220px">
          <el-option v-for="sp in specialtyOptions" :key="sp" :label="sp" :value="sp" />
        </el-select>
      </div>

      <div class="grid">
        <el-card v-for="c in list" :key="c.id" class="item" shadow="hover">
          <h3>{{ c.name }}</h3>
          <p class="line">{{ c.title }}</p>
          <p class="line">擅长：{{ c.specialty }}</p>
          <p class="line">评分：{{ c.rating }} / 5.0</p>
          <div class="actions">
            <el-button size="small" @click="view(c.id)">查看详情</el-button>
            <el-button size="small" type="primary" @click="book(c)">立即预约</el-button>
          </div>
        </el-card>
      </div>

      <el-dialog v-model="showBook" width="520px" title="立即预约">
        <el-form :model="bookForm" label-width="90px">
          <el-form-item label="咨询师">
            <el-input :model-value="selectedCounselor?.name || '-'" disabled />
          </el-form-item>
          <el-form-item label="开始时间">
            <el-select v-model="bookForm.startTime" placeholder="请选择可预约时间" style="width: 100%">
              <el-option v-for="slot in slots" :key="slot" :label="slot" :value="slot" />
            </el-select>
          </el-form-item>
          <el-form-item label="时长(分钟)">
            <el-input-number v-model="bookForm.duration" :min="30" :max="120" :step="30" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="bookForm.note" type="textarea" :rows="2" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showBook = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitBooking">提交预约</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const list = ref([])
const keyword = ref('')
const gender = ref('')
const specialty = ref('')
const specialtyOptions = ref([])
const loading = ref(false)
const showBook = ref(false)
const submitting = ref(false)
const slots = ref([])
const selectedCounselor = ref(null)
const bookForm = ref({ startTime: '', duration: 30, note: '' })
const router = useRouter()

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/counselors', { params: { keyword: keyword.value, gender: gender.value, specialty: specialty.value } })
    const body = res.data || {}
    const rows = body.data || body
    list.value = Array.isArray(rows) ? rows : []
    specialtyOptions.value = Array.from(new Set(list.value.map(x => x.specialty).filter(Boolean)))
  } finally {
    loading.value = false
  }
}

const view = (id) => router.push(`/student/counselor/${id}`)
const book = async (c) => {
  selectedCounselor.value = c
  bookForm.value = { startTime: '', duration: 30, note: '' }
  slots.value = []
  showBook.value = true
  try {
    const res = await axios.get(`/api/counselors/${c.id}`)
    const body = res.data || {}
    const detail = body.data || body
    slots.value = detail.availableSlots || []
  } catch (_) {
    window.$message?.error('加载预约时段失败')
  }
}

const submitBooking = async () => {
  const userId = Number(localStorage.getItem('user_id') || 0)
  if (!userId) {
    window.$message?.error('请先登录')
    router.push('/')
    return
  }
  if (!selectedCounselor.value) return
  if (!bookForm.value.startTime) {
    window.$message?.error('请选择预约时间')
    return
  }
  submitting.value = true
  try {
    await axios.post('/api/appointments', {
      studentId: userId,
      counselorId: selectedCounselor.value.id,
      startTime: bookForm.value.startTime,
      duration: bookForm.value.duration,
      note: bookForm.value.note
    })
    window.$message?.success('预约提交成功')
    showBook.value = false
    router.push('/student/appointments')
  } finally {
    submitting.value = false
  }
}

const goHome = () => router.push('/student/home')

onMounted(load)
</script>

<style scoped>
.page { max-width:900px; margin:24px auto }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px }
.filters { display:flex; gap:12px; margin-bottom:14px }
.grid {
  display:grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap:12px;
}
.item h3 { margin: 0 0 6px }
.line { margin: 3px 0; color:#666 }
.actions { margin-top: 10px; display:flex; justify-content: flex-end; gap:8px }
</style>
