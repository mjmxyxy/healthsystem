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
import CounselorShell from './components/CounselorShell.vue'
import CounselorDashboard from './pages/CounselorDashboard.vue'
import CounselorAppointments from './pages/CounselorAppointments.vue'
import CounselorChat from './pages/CounselorChat.vue'
import CounselorStudents from './pages/CounselorStudents.vue'
import CounselorReports from './pages/CounselorReports.vue'
import CounselorProfile from './pages/CounselorProfile.vue'
import CounselorArticles from './pages/CounselorArticles.vue'
import AdminHome from './pages/AdminHome.vue'
import AdminShell from './components/AdminShell.vue'
import AdminStudents from './pages/AdminStudents.vue'
import AdminCounselors from './pages/AdminCounselors.vue'
import AdminScaleQuestions from './pages/AdminScaleQuestions.vue'
import AdminAppointments from './pages/AdminAppointments.vue'
import AdminArticlesReview from './pages/AdminArticlesReview.vue'
import AdminProfile from './pages/AdminProfile.vue'
import Register from './pages/Register.vue'
import ResetPassword from './pages/ResetPassword.vue'
import ChatAI from './pages/ChatAI.vue'
import CounselorList from './pages/CounselorList.vue'
import CounselorDetail from './pages/CounselorDetail.vue'
import MyAppointments from './pages/MyAppointments.vue'
import ChatRoom from './pages/ChatRoom.vue'
import StudentProfile from './pages/StudentProfile.vue'
import StudentArticles from './pages/StudentArticles.vue'
import StudentArticleDetail from './pages/StudentArticleDetail.vue'
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
  { path: 'articles', component: StudentArticles },
  { path: 'articles/:id', component: StudentArticleDetail },
  { path: 'profile', component: StudentProfile },
  { path: 'ai-chat', component: ChatAI }
]

const counselorChildren = [
  { path: '', redirect: '/counselor/dashboard' },
  { path: 'dashboard', component: CounselorDashboard },
  { path: 'appointments', component: CounselorAppointments },
  { path: 'chat/:studentId', component: CounselorChat },
  { path: 'students', component: CounselorStudents },
  { path: 'reports', component: CounselorReports },
  { path: 'articles', component: CounselorArticles },
  { path: 'profile', component: CounselorProfile }
]

const adminChildren = [
  { path: '', redirect: '/admin/dashboard' },
  { path: 'dashboard', component: AdminHome },
  { path: 'students', component: AdminStudents },
  { path: 'counselors', component: AdminCounselors },
  { path: 'scales', component: AdminScaleQuestions },
  { path: 'appointments', component: AdminAppointments },
  { path: 'articles', component: AdminArticlesReview },
  { path: 'profile', component: AdminProfile }
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
  { path: '/counselor/home', redirect: '/counselor/dashboard' },
  { path: '/counselor', component: CounselorShell, children: counselorChildren },
  { path: '/admin/home', redirect: '/admin/dashboard' },
  { path: '/admin', component: AdminShell, children: adminChildren }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token') || localStorage.getItem('jwt_token')
  const role = localStorage.getItem('role')
  if (to.path.startsWith('/student') && !token) {
    return '/'
  }
  if (to.path.startsWith('/counselor') && !token) {
    return '/'
  }
  if (to.path.startsWith('/counselor') && role && role !== 'counselor') {
    return '/'
  }
  if (to.path.startsWith('/admin') && !token) {
    return '/'
  }
  if (to.path.startsWith('/admin') && role && role !== 'admin') {
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
