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
        <el-select v-model="specialty" placeholder="擅长领域筛选" clearable style="width:220px">
          <el-option v-for="sp in specialtyOptions" :key="sp" :label="sp" :value="sp" />
        </el-select>
      </div>

      <div class="grid">
        <el-card v-for="c in filteredList" :key="c.id" class="item" shadow="hover">
          <div class="top">
            <div class="avatar-wrap">
              <div class="avatar">{{ avatarText(c.name) }}</div>
              <span class="online-dot"></span>
            </div>
            <div class="basic">
              <div class="name-row">
                <h3>{{ c.name }}</h3>
                <div class="rating">⭐ {{ Number(c.rating || 0).toFixed(1) }}</div>
                <el-tag v-if="Number(c.rating || 0) >= 4.5" type="warning" effect="light">好评</el-tag>
              </div>
              <p class="line">{{ c.title }} | 从业 {{ years(c.id) }} 年</p>
              <div class="skills">
                <span class="skill" v-for="s in skillList(c.specialty)" :key="`${c.id}-${s}`">{{ s }}</span>
              </div>
            </div>
            <el-tag type="success" effect="light">在线</el-tag>
          </div>

          <div class="actions">
            <el-button size="small" @click="view(c.id)">查看详情</el-button>
            <el-button size="small" type="primary" @click="book(c)">立即预约</el-button>
          </div>
        </el-card>
      </div>

      <el-dialog v-model="showBook" width="520px" title="立即预约">
        <div class="booking-summary">
          <div class="avatar-sm">{{ avatarText(selectedCounselor?.name || '-') }}</div>
          <div>
            <div class="sum-name">{{ selectedCounselor?.name || '-' }}</div>
            <div class="sum-sub">{{ selectedCounselor?.title || '-' }} · {{ selectedCounselor?.specialty || '-' }}</div>
          </div>
        </div>

        <div class="picker-title">选择日期</div>
        <div class="date-row">
          <button
            v-for="d in dateOptions"
            :key="d.value"
            class="date-btn"
            :class="{ active: bookForm.date === d.value }"
            @click="bookForm.date = d.value"
          >
            <span>{{ d.label }}</span>
            <small>{{ d.week }}</small>
          </button>
        </div>

        <div class="picker-title">选择时段</div>
        <div class="slot-grid">
          <button
            v-for="t in timeOptions"
            :key="t"
            class="slot-btn"
            :class="{ active: bookForm.time === t }"
            :disabled="!isTimeAvailable(t)"
            @click="bookForm.time = t"
          >
            {{ t }}
          </button>
        </div>

        <el-form :model="bookForm" label-width="90px" style="margin-top:12px">
          <el-form-item label="时长(分钟)">
            <el-input-number v-model="bookForm.duration" :min="30" :max="120" :step="30" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="bookForm.note" type="textarea" :rows="3" />
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
import { computed, onMounted, ref } from 'vue'
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
const bookForm = ref({ date: '', time: '', duration: 30, note: '' })
const router = useRouter()
const timeOptions = ['09:00', '10:00', '11:00', '14:00', '15:00', '16:00', '19:00', '20:00', '21:00']

const filteredList = computed(() => {
  const k = keyword.value.trim()
  return list.value.filter((c) => {
    const hitK = !k || String(c.name || '').includes(k) || String(c.account || '').includes(k)
    const hitSp = !specialty.value || skillList(c.specialty).includes(specialty.value)
    return hitK && hitSp
  })
})

const dateOptions = computed(() => {
  const arr = []
  const now = new Date()
  const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  for (let i = 0; i < 8; i++) {
    const d = new Date(now)
    d.setDate(now.getDate() + i)
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    arr.push({
      value: `${y}-${m}-${day}`,
      label: i === 0 ? '今天' : `${m}/${day}`,
      week: weeks[d.getDay()]
    })
  }
  return arr
})

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/counselors', { params: { keyword: keyword.value, gender: gender.value, specialty: specialty.value } })
    const body = res.data || {}
    const rows = body.data || body
    list.value = Array.isArray(rows) ? rows : []
    specialtyOptions.value = Array.from(new Set(list.value.flatMap(x => skillList(x.specialty)).filter(Boolean)))
  } finally {
    loading.value = false
  }
}

const avatarText = (name) => String(name || '咨').slice(-1)
const years = (id) => 2 + (Number(id || 0) % 8)
const skillList = (sp) => String(sp || '').split(/[,，、\s]+/).filter(Boolean)

const toDateTime = (slot) => {
  const d = new Date(slot)
  if (Number.isNaN(d.getTime())) return null
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${hh}:${mm}`
}

const isTimeAvailable = (time) => {
  if (!bookForm.value.date) return false
  const key = `${bookForm.value.date} ${time}`
  return slots.value.some((s) => toDateTime(s) === key)
}

const view = (id) => router.push(`/student/counselor/${id}`)
const book = async (c) => {
  selectedCounselor.value = c
  bookForm.value = { date: dateOptions.value[0]?.value || '', time: '', duration: 30, note: '' }
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
  if (!bookForm.value.date || !bookForm.value.time) {
    window.$message?.error('请选择预约日期和时段')
    return
  }
  const startTime = `${bookForm.value.date}T${bookForm.value.time}:00`
  submitting.value = true
  try {
    await axios.post('/api/appointments', {
      studentId: userId,
      counselorId: selectedCounselor.value.id,
      startTime,
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
.page { max-width:1080px; margin:0 auto }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px }
.filters { display:flex; gap:12px; margin-bottom:14px; flex-wrap: wrap }
.grid {
  display:grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap:12px;
}
.item { border-radius: var(--radius-xl); }
.top { display:flex; align-items:flex-start; gap:12px; }
.avatar-wrap { position: relative; }
.avatar {
  width:80px;
  height:80px;
  border-radius:50%;
  background: linear-gradient(135deg, var(--color-student), var(--color-primary));
  color:#fff;
  display:flex;
  align-items:center;
  justify-content:center;
  font-size:28px;
  font-weight:700;
}
.online-dot {
  width:8px;
  height:8px;
  border-radius:50%;
  background:#10B981;
  position:absolute;
  right:4px;
  bottom:4px;
  box-shadow: 0 0 0 2px #fff;
}
.basic { flex:1; }
.name-row { display:flex; align-items:center; gap:8px; flex-wrap: wrap; }
.item h3 { margin: 0; }
.rating { color: #f59e0b; font-size: var(--font-size-sm); font-weight: var(--font-weight-semibold); }
.line { margin: 6px 0; color: var(--color-text-secondary) }
.skills { display:flex; gap:6px; flex-wrap: wrap; }
.skill {
  border-radius: 999px;
  padding: 3px 10px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
  transition: all .2s ease;
}
.skill:hover {
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
}
.actions { margin-top: 10px; display:flex; justify-content: flex-end; gap:8px }

.booking-summary {
  display:flex;
  gap:10px;
  align-items:center;
  margin-bottom: 12px;
}
.avatar-sm {
  width:44px;
  height:44px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-student), var(--color-primary));
  color:#fff;
  display:flex;
  align-items:center;
  justify-content:center;
  font-weight:700;
}
.sum-name { color: var(--color-text-primary); font-weight: var(--font-weight-semibold); }
.sum-sub { color: var(--color-text-secondary); font-size: var(--font-size-sm); }

.picker-title { margin: 8px 0; color: var(--color-text-primary); font-weight: var(--font-weight-medium); }

.date-row {
  display:flex;
  gap:8px;
  overflow-x:auto;
  padding-bottom: 2px;
}
.date-btn {
  border:1px solid var(--color-border);
  border-radius: var(--radius-md);
  background:#fff;
  min-width:78px;
  height:54px;
  cursor:pointer;
  display:flex;
  flex-direction:column;
  justify-content:center;
  align-items:center;
}
.date-btn.active {
  border-color: var(--color-student);
  background: rgba(91, 124, 245, .12);
  color: var(--color-student);
}

.slot-grid {
  margin-top: 8px;
  display:grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap:8px;
}
.slot-btn {
  height:40px;
  border-radius: var(--radius-md);
  border:1px solid var(--color-border);
  background:#fff;
  cursor:pointer;
}
.slot-btn.active {
  border-color: var(--color-student);
  background: rgba(91, 124, 245, .12);
  color: var(--color-student);
}
.slot-btn:disabled {
  cursor:not-allowed;
  background: #f3f4f6;
  color: #9ca3af;
}
</style>
