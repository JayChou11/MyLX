<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学生姓名" prop="studentName">
        <el-input v-model="queryParams.studentName" placeholder="请输入学生姓名" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option label="待班主任审批" value="0" />
          <el-option label="待教务处审批" value="1" />
          <el-option label="已通过" value="2" />
          <el-option label="已拒绝" value="3" />
          <el-option label="已撤回" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="申请时间" style="width: 308px">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:student:transferApprove:add']">新增申请</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:student:transferApprove:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:student:transferApprove:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 申请列表 -->
    <el-table v-loading="loading" :data="applyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="申请ID" align="center" prop="applyId" width="80" />
      <el-table-column label="学生姓名" align="center" prop="studentName" width="100" />
      <el-table-column label="原班级" align="center" prop="beforeClassName" min-width="120">
        <template #default="scope">
          <span>{{ scope.row.beforeGrade }} {{ scope.row.beforeClassName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="目标班级" align="center" prop="afterClassName" min-width="120">
        <template #default="scope">
          <span>{{ scope.row.afterGrade }} {{ scope.row.afterClassName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="申请原因" align="center" prop="applyReason" min-width="150" show-overflow-tooltip />
      <el-table-column label="申请人" align="center" prop="applyBy" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '0'" type="info">待班主任审批</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="warning">待教务处审批</el-tag>
          <el-tag v-else-if="scope.row.status === '2'" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.status === '3'" type="danger">已拒绝</el-tag>
          <el-tag v-else-if="scope.row.status === '4'" type="info">已撤回</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请时间" align="center" prop="applyTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.applyTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Check" @click="handleApprove(scope.row)" v-if="canApprove(scope.row)" v-hasPermi="['system:student:transferApprove:approve']">审批</el-button>
          <el-button link type="warning" icon="RefreshLeft" @click="handleCancelApply(scope.row)" v-if="canCancel(scope.row)" v-hasPermi="['system:student:transferApprove:cancel']">撤回</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="canDelete(scope.row)" v-hasPermi="['system:student:transferApprove:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增申请对话框 -->
    <el-dialog title="新增转班申请" v-model="addOpen" width="600px" append-to-body @close="cancelAdd">
      <el-form ref="addRef" :model="addForm" :rules="addRules" label-width="92px">
        <el-form-item label="选择学生" prop="studentId">
          <el-select v-model="addForm.studentId" filterable remote reserve-keyword placeholder="请输入学生姓名或学号搜索" :remote-method="searchStudent" :loading="studentLoading" style="width: 100%">
            <el-option v-for="item in studentOptions" :key="item.studentId" :label="item.studentName + ' (' + item.studentNo + ')'" :value="item.studentId" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标年级" prop="afterGrade">
          <el-select v-model="addForm.afterGrade" placeholder="请选择目标年级" style="width: 100%" @change="handleGradeChange">
            <el-option v-for="item in gradeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标班级" prop="afterClassId">
          <el-select v-model="addForm.afterClassId" placeholder="请先选择目标年级" style="width: 100%">
            <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请原因" prop="applyReason">
          <el-input v-model="addForm.applyReason" type="textarea" :rows="4" placeholder="请输入转班申请原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAdd">确 定</el-button>
          <el-button @click="cancelAdd">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="转班申请详情" v-model="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请ID">{{ detailData.applyId }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="原班级">{{ detailData.beforeGrade }} {{ detailData.beforeClassName }}</el-descriptions-item>
        <el-descriptions-item label="目标班级">{{ detailData.afterGrade }} {{ detailData.afterClassName }}</el-descriptions-item>
        <el-descriptions-item label="申请原因" :span="2">{{ detailData.applyReason }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detailData.applyBy }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ parseTime(detailData.applyTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag v-if="detailData.status === '0'" type="info">待班主任审批</el-tag>
          <el-tag v-else-if="detailData.status === '1'" type="warning">待教务处审批</el-tag>
          <el-tag v-else-if="detailData.status === '2'" type="success">已通过</el-tag>
          <el-tag v-else-if="detailData.status === '3'" type="danger">已拒绝</el-tag>
          <el-tag v-else-if="detailData.status === '4'" type="info">已撤回</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.rejectReason" label="拒绝原因" :span="2">
          <span style="color: #f56c6c">{{ detailData.rejectReason }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 审批记录时间线 -->
      <div v-if="detailData.approveList && detailData.approveList.length > 0" style="margin-top: 20px;">
        <h4>审批记录</h4>
        <el-timeline>
          <el-timeline-item v-for="(item, index) in detailData.approveList" :key="index" :timestamp="parseTime(item.approveTime)" placement="top">
            <el-card>
              <h4>{{ item.approveLevel === 1 ? '班主任审批' : '教务处审批' }}</h4>
              <p>审批人：{{ item.approveBy }}</p>
              <p>审批结果：
                <el-tag v-if="item.approveResult === '0'" type="success">通过</el-tag>
                <el-tag v-else type="danger">拒绝</el-tag>
              </p>
              <p v-if="item.approveRemark">审批意见：{{ item.approveRemark }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog title="审批转班申请" v-model="approveOpen" width="500px" append-to-body @close="cancelApprove">
      <el-form ref="approveRef" :model="approveForm" :rules="approveRules" label-width="88px">
        <el-form-item label="审批结果" prop="approveResult">
          <el-radio-group v-model="approveForm.approveResult">
            <el-radio label="0">通过</el-radio>
            <el-radio label="1">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见" prop="approveRemark">
          <el-input v-model="approveForm.approveRemark" type="textarea" :rows="4" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitApprove">确 定</el-button>
          <el-button @click="cancelApprove">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TransferApprove">
import { listTransferApply, getTransferApply, addTransferApply, approveTransferApply, cancelTransferApply, delTransferApply, exportTransferApply } from '@/api/system/transferApply'
import { listStudent } from '@/api/system/student'
import { listClass } from '@/api/system/class'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.roles.includes('admin'))
const isClassTeacher = computed(() => userStore.roles.includes('class_teacher'))
const isAcademicOffice = computed(() => userStore.roles.includes('academic_office'))
const loginUserName = computed(() => userStore.name)

// 列表数据
const applyList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const dateRange = ref([])

// 新增相关
const addOpen = ref(false)
const addRef = ref(null)
const studentLoading = ref(false)
const studentOptions = ref([])
const gradeOptions = ref(['初一', '初二', '初三', '高一', '高二', '高三'])
const classOptions = ref([])

// 详情相关
const detailOpen = ref(false)
const detailData = ref({})

// 审批相关
const approveOpen = ref(false)
const approveRef = ref(null)
const approveForm = ref({
  applyId: null,
  approveResult: '0',
  approveRemark: ''
})

// 查询参数
const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentName: undefined,
    status: undefined
  },
  addForm: {
    studentId: undefined,
    afterGrade: undefined,
    afterClassId: undefined,
    applyReason: undefined
  },
  addRules: {
    studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
    afterGrade: [{ required: true, message: '请选择目标年级', trigger: 'change' }],
    afterClassId: [{ required: true, message: '请选择目标班级', trigger: 'change' }],
    applyReason: [{ required: true, message: '请输入申请原因', trigger: 'blur' }]
  },
  approveRules: {
    approveResult: [{ required: true, message: '请选择审批结果', trigger: 'change' }]
  }
})

const { queryParams, addForm, addRules, approveRules } = toRefs(data)

/** 查询列表 */
function getList() {
  loading.value = true
  listTransferApply(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    applyList.value = response.rows
    total.value = response.total
  }).finally(() => {
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.applyId)
  single.value = selection.length !== 1
  multiple.value = !selection.length || selection.some(item => !canDelete(item))
}

/** 是否可以审批 */
function canApprove(row) {
  if (row.status === '0') {
    return isAdmin.value || isClassTeacher.value
  }
  return row.status === '1' && (isAdmin.value || isAcademicOffice.value)
}

/** 是否可以撤回 */
function canCancel(row) {
  const canOperateStatus = row.status === '0' || row.status === '1'
  if (!canOperateStatus) {
    return false
  }
  return isAdmin.value || row.applyBy === loginUserName.value
}

/** 是否可以删除 */
function canDelete(row) {
  const canOperateStatus = row.status === '0' || row.status === '3' || row.status === '4'
  if (!canOperateStatus) {
    return false
  }
  return isAdmin.value || isAcademicOffice.value || row.applyBy === loginUserName.value
}

/** 搜索学生 */
function searchStudent(query) {
  if (query !== '') {
    studentLoading.value = true
    listStudent({ studentName: query }).then(response => {
      studentOptions.value = response.rows
      studentLoading.value = false
    })
  } else {
    studentOptions.value = []
  }
}

/** 年级变化 */
function handleGradeChange(grade) {
  addForm.value.afterClassId = undefined
  classOptions.value = []
  if (grade) {
    listClass({ grade: grade }).then(response => {
      classOptions.value = response.rows
    })
  }
}

/** 新增按钮操作 */
function handleAdd() {
  proxy.resetForm("addRef")
  addForm.value.studentId = undefined
  addForm.value.afterGrade = undefined
  addForm.value.afterClassId = undefined
  addForm.value.applyReason = undefined
  studentOptions.value = []
  classOptions.value = []
  addOpen.value = true
}

/** 取消新增 */
function cancelAdd() {
  addOpen.value = false
  proxy.resetForm("addRef")
  studentOptions.value = []
  classOptions.value = []
}

/** 提交新增 */
function submitAdd() {
  proxy.$refs["addRef"].validate(valid => {
    if (valid) {
      addTransferApply(addForm.value).then(response => {
        proxy.$modal.msgSuccess("申请提交成功")
        addOpen.value = false
        getList()
      })
    }
  })
}

/** 详情按钮操作 */
function handleDetail(row) {
  getTransferApply(row.applyId).then(response => {
    detailData.value = response.data
    detailOpen.value = true
  })
}

/** 审批按钮操作 */
function handleApprove(row) {
  approveForm.value.applyId = row.applyId
  approveForm.value.approveResult = '0'
  approveForm.value.approveRemark = ''
  approveOpen.value = true
}

/** 取消审批 */
function cancelApprove() {
  approveOpen.value = false
  proxy.resetForm("approveRef")
}

/** 提交审批 */
function submitApprove() {
  proxy.$refs["approveRef"].validate(valid => {
    if (valid) {
      const msg = approveForm.value.approveResult === '0' ? '确认通过该申请？' : '确认拒绝该申请？'
      proxy.$modal.confirm(msg).then(function() {
        return approveTransferApply(approveForm.value)
      }).then(() => {
        proxy.$modal.msgSuccess("审批成功")
        approveOpen.value = false
        getList()
      }).catch(() => {})
    }
  })
}

/** 撤回按钮操作 */
function handleCancelApply(row) {
  proxy.$modal.confirm('是否确认撤回转班申请编号为"' + row.applyId + '"的数据项？').then(function() {
    return cancelTransferApply(row.applyId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("撤回成功")
  }).catch(() => {})
}

/** 删除按钮操作 */
function handleDelete(row) {
  const applyIds = row.applyId || ids.value
  proxy.$modal.confirm('是否确认删除转班申请编号为"' + applyIds + '"的数据项？').then(function() {
    return delTransferApply(applyIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("system/student/transferApply/export", {
    ...queryParams.value
  }, `transferApply_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
.tip-text {
  margin-bottom: 20px;
  color: #909399;
  font-size: 14px;
}
</style>
