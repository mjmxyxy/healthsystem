import { createApp } from 'vue'
import { h } from 'vue'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory, RouterView } from 'vue-router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './student-theme.css'
import axios from 'axios'
import Login from './pages/Login.vue'
import StudentShell from './components/StudentShell.vue'
import StudentHome from './pages/StudentHome.vue'
import ScaleList from './pages/ScaleList.vue'
import TestPage from './pages/TestPage.vue'
import CounselorHome from './pages/CounselorHome.vue'
import AdminHome from './pages/AdminHome.vue'
import Register from './pages/Register.vue'
import ResetPassword from './pages/ResetPassword.vue'
import ChatAI from './pages/ChatAI.vue'
import CounselorList from './pages/CounselorList.vue'
import CounselorDetail from './pages/CounselorDetail.vue'
import MyAppointments from './pages/MyAppointments.vue'
import ChatRoom from './pages/ChatRoom.vue'
import StudentProfile from './pages/StudentProfile.vue'
import ReportDetail from './pages/ReportDetail.vue'
import History from './pages/History.vue'

const studentChildren = [
  { path: '', redirect: '/student/home' },
  { path: 'home', component: StudentHome },
  { path: 'scales', component: ScaleList },
  { path: 'test/:id', component: TestPage },
  { path: 'assessments', component: History },
  { path: 'assessment/:id', component: ReportDetail },
  { path: 'counselors', component: CounselorList },
  { path: 'counselor/:id', component: CounselorDetail },
  { path: 'appointments', component: MyAppointments },
  { path: 'chat/:id', component: ChatRoom },
  { path: 'profile', component: StudentProfile },
  { path: 'ai-chat', component: ChatAI }
]

const routes = [
  { path: '/', component: Login },
  { path: '/register', component: Register },
  { path: '/reset', component: ResetPassword },
  { path: '/student', component: StudentShell, children: studentChildren },
  { path: '/student/history', redirect: '/student/assessments' },
  { path: '/student/report/:id', redirect: (to: any) => `/student/assessment/${to.params.id}` },
  { path: '/student/my-appointments', redirect: '/student/appointments' },
  { path: '/counselors', redirect: '/student/counselors' },
  { path: '/counselor/:id', redirect: (to: any) => `/student/counselor/${to.params.id}` },
  { path: '/chat/with/:counselorId', redirect: (to: any) => `/student/chat/${to.params.counselorId}` },
  { path: '/counselor/home', component: CounselorHome },
  { path: '/admin/home', component: AdminHome }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token') || localStorage.getItem('jwt_token')
  if (to.path.startsWith('/student') && !token) {
    return '/'
  }
  return true
})

const token = localStorage.getItem('token') || localStorage.getItem('jwt_token')
if (token) {
  axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
}
axios.interceptors.request.use((config) => {
  const cachedToken = localStorage.getItem('token') || localStorage.getItem('jwt_token')
  if (cachedToken) {
    config.headers = config.headers || {}
    config.headers['Authorization'] = `Bearer ${cachedToken}`
  }
  return config
})

const app = createApp({
  render: () => h(RouterView)
})
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
