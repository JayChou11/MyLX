<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="考试名称" prop="examName">
        <el-input v-model="queryParams.examName" placeholder="请输入考试名称" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="年级" prop="grade">
        <el-input v-model="queryParams.grade" placeholder="请输入年级" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="学期" prop="semester">
        <el-input v-model="queryParams.semester" placeholder="请输入学期" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="examStatus">
        <el-select v-model="queryParams.examStatus" placeholder="请选择状态" clearable style="width: 160px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:exam:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:exam:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:exam:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:exam:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="examList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="考试ID" prop="examId" align="center" width="90" />
      <el-table-column label="考试名称" prop="examName" align="center" min-width="180" show-overflow-tooltip />
      <el-table-column label="年级" prop="grade" align="center" width="110" />
      <el-table-column label="学期" prop="semester" align="center" width="130" />
      <el-table-column label="考试日期" align="center" width="130"><template #default="{ row }">{{ parseTime(row.examDate, '{y}-{m}-{d}') }}</template></el-table-column>
      <el-table-column label="状态" align="center" width="110"><template #default="{ row }"><el-tag :type="statusTag(row.examStatus)">{{ statusLabel(row.examStatus) }}</el-tag></template></el-table-column>
      <el-table-column label="备注" prop="remark" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)" v-hasPermi="['system:exam:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:exam:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="examRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="考试名称" prop="examName"><el-input v-model="form.examName" maxlength="100" show-word-limit placeholder="请输入考试名称" /></el-form-item>
        <el-form-item label="年级" prop="grade"><el-input v-model="form.grade" maxlength="20" placeholder="例如：2024级" /></el-form-item>
        <el-form-item label="学期" prop="semester"><el-input v-model="form.semester" maxlength="20" placeholder="例如：2025-2026-1" /></el-form-item>
        <el-form-item label="考试日期" prop="examDate"><el-date-picker v-model="form.examDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择考试日期" style="width: 100%" /></el-form-item>
        <el-form-item v-if="form.examId == null" label="状态" prop="examStatus"><el-select v-model="form.examStatus" disabled style="width: 100%"><el-option label="草稿" value="0" /></el-select></el-form-item>
        <el-form-item v-else label="当前状态"><el-tag :type="statusTag(form.examStatus)">{{ statusLabel(form.examStatus) }}</el-tag></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请输入备注，可选" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="SysExam">
import { parseTime } from '@/utils/ruoyi'
import { listExam, getExam, addExam, updateExam, delExam } from '@/api/system/exam'

const { proxy } = getCurrentInstance()
const examList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const queryParams = ref({ pageNum: 1, pageSize: 10, examName: undefined, grade: undefined, semester: undefined, examStatus: undefined })
const form = ref({})
const statusOptions = [
  { value: '0', label: '草稿', tag: 'info' },
  { value: '1', label: '成绩录入中', tag: 'warning' },
  { value: '2', label: '已发布', tag: 'success' },
  { value: '3', label: '已归档', tag: '' }
]
const rules = {
  examName: [{ required: true, message: '考试名称不能为空', trigger: 'blur' }, { max: 100, message: '考试名称不能超过100个字符', trigger: 'blur' }],
  grade: [{ required: true, message: '年级不能为空', trigger: 'blur' }, { max: 20, message: '年级不能超过20个字符', trigger: 'blur' }],
  semester: [{ required: true, message: '学期不能为空', trigger: 'blur' }, { max: 20, message: '学期不能超过20个字符', trigger: 'blur' }],
  examDate: [{ required: true, message: '考试日期不能为空', trigger: 'change' }],
  examStatus: [{ required: true, message: '状态不能为空', trigger: 'change' }],
  remark: [{ max: 200, message: '备注不能超过200个字符', trigger: 'blur' }]
}

function statusLabel(value) { return statusOptions.find(item => item.value === String(value))?.label || value || '-' }
function statusTag(value) { return statusOptions.find(item => item.value === String(value))?.tag || 'info' }
function getList() {
  loading.value = true
  listExam(queryParams.value).then(response => { examList.value = response.rows || []; total.value = response.total || 0 }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.examId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { examId: undefined, examName: '', grade: '', semester: '', examDate: '', examStatus: '0', remark: '' }; proxy.resetForm('examRef') }
function handleAdd() { reset(); title.value = '新增考试'; open.value = true }
function handleUpdate(row) {
  const examId = row?.examId || ids.value[0]
  if (!examId) return
  reset()
  getExam(examId).then(response => { form.value = { ...form.value, ...response.data }; open.value = true; title.value = '修改考试' })
}
function cancel() { open.value = false; reset() }
function submitForm() {
  proxy.$refs.examRef.validate(valid => {
    if (!valid) return
    const editing = form.value.examId != null
    const request = editing ? updateExam : addExam
    const payload = editing ? (({ examStatus, ...basicInfo }) => basicInfo)(form.value) : form.value
    request(payload).then(() => { proxy.$modal.msgSuccess(editing ? '修改成功' : '新增成功'); open.value = false; getList() })
  })
}
function handleDelete(row) {
  const examIds = row?.examId ? [row.examId] : [...ids.value]
  if (!examIds.length) return
  proxy.$modal.confirm(`是否确认删除考试编号为"${examIds.join(',')}"的数据项？`).then(() => delExam(examIds.join(','))).then(() => { proxy.$modal.msgSuccess('删除成功'); getList() }).catch(() => {})
}
function handleExport() { proxy.download('system/exam/export', { ...queryParams.value }, `exam_${Date.now()}.xlsx`) }
getList()
</script>
