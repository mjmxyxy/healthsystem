<template>
  <div class="page admin-page">
    <el-card>
      <div class="head">
        <h2>咨询师管理</h2>
        <div class="ops">
          <el-input v-model="keyword" placeholder="账号/姓名" clearable style="width:220px" @keyup.enter="load" />
          <el-button @click="load">查询</el-button>
        </div>
      </div>

      <div class="batch-floating" v-if="selectedRows.length">
        已选中 {{ selectedRows.length }} 项
        <el-button size="small" @click="batchApprove">批量通过</el-button>
        <el-button size="small" type="danger" @click="batchOffShelf">批量下架</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" @selection-change="selectedRows = $event">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="account" label="账号" width="150" />
        <el-table-column prop="name" label="姓名" width="140" />
        <el-table-column prop="gender" label="性别" width="90" />
        <el-table-column prop="verified" label="审核" width="90">
          <template #default="{ row }">
            <el-tag :type="row.verified ? 'success' : 'warning'">{{ row.verified ? '通过' : '待审' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="disabled" label="上下架" width="90">
          <template #default="{ row }">
            <el-tag :type="row.disabled ? 'danger' : 'success'">{{ row.disabled ? '下架' : '上架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" min-width="320" fixed="right">
          <template #default="{ row }">
            <div class="table-actions cols-4">
              <el-button size="small" type="success" @click="review(row, 1)">通过</el-button>
              <el-button size="small" type="warning" @click="review(row, 0)">驳回</el-button>
              <el-button size="small" :type="row.disabled ? 'success' : 'danger'" @click="shelve(row)">
                {{ row.disabled ? '上架' : '下架' }}
              </el-button>
              <el-button size="small" @click="openEdit(row)">编辑</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showEdit" title="编辑咨询师" width="460px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="editForm.gender" style="width:100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
            <el-option label="未知" value="未知" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const keyword = ref('')
const rows = ref([])
const selectedRows = ref([])
const showEdit = ref(false)
const editForm = reactive({ id: null, name: '', gender: '' })

const parseData = (res) => {
  const body = res?.data || {}
  return body.data || body
}

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/counselors', { params: { keyword: keyword.value || undefined } })
    rows.value = parseData(res) || []
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '咨询师列表加载失败')
  } finally {
    loading.value = false
  }
}

const review = async (row, approved) => {
  try {
    await axios.post(`/api/admin/counselors/${row.id}/review`, { approved })
    ElMessage.success(approved ? '已通过' : '已驳回')
    await load()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '审核失败')
  }
}

const shelve = async (row) => {
  const disabled = row.disabled ? 0 : 1
  try {
    await axios.post(`/api/admin/counselors/${row.id}/shelve`, { disabled })
    ElMessage.success(disabled ? '已下架' : '已上架')
    await load()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '上下架失败')
  }
}

const openEdit = (row) => {
  editForm.id = row.id
  editForm.name = row.name || ''
  editForm.gender = row.gender || '未知'
  showEdit.value = true
}

const saveEdit = async () => {
  try {
    await axios.put(`/api/admin/counselors/${editForm.id}`, { name: editForm.name, gender: editForm.gender })
    ElMessage.success('保存成功')
    showEdit.value = false
    await load()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '保存失败')
  }
}

const batchApprove = async () => {
  for (const row of selectedRows.value) {
    await axios.post(`/api/admin/counselors/${row.id}/review`, { approved: 1 })
  }
  ElMessage.success('批量通过成功')
  await load()
}

const batchOffShelf = async () => {
  for (const row of selectedRows.value) {
    await axios.post(`/api/admin/counselors/${row.id}/shelve`, { disabled: 1 })
  }
  ElMessage.success('批量下架成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px; gap: 12px; }
.ops { display:flex; gap: 8px; flex-wrap: wrap; }
</style>
