<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学号" prop="studentNo">
        <el-input
          v-model="queryParams.studentNo"
          placeholder="请输入学号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="姓名" prop="studentName">
        <el-input
          v-model="queryParams.studentName"
          placeholder="请输入姓名"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-select v-model="queryParams.gender" placeholder="请选择性别" clearable style="width: 200px">
          <el-option
            v-for="dict in sys_user_sex"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="年级" prop="grade">
        <el-select v-model="queryParams.grade" placeholder="请选择年级" clearable style="width: 200px" @change="handleGradeChange">
          <el-option
            v-for="item in gradeOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="班级" prop="classId">
        <el-select v-model="queryParams.classId" placeholder="请选择班级" clearable style="width: 200px">
          <el-option
            v-for="item in classOptions"
            :key="item.classId"
            :label="item.className"
            :value="item.classId"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:student:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['system:student:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Edit" :disabled="multiple" @click="handleTransferClass" v-hasPermi="['system:student:edit']">批量调班</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Tickets" @click="goTransferRecord" v-hasPermi="['system:student:transfer:list']">调班记录</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:student:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:student:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExportSelected" v-hasPermi="['system:student:export']">选择导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['system:student:import']">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="DataAnalysis" @click="handleClassStats" v-hasPermi="['system:student:list']">班级统计</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="studentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="学生ID" align="center" prop="studentId" width="90" />
      <el-table-column label="学号" align="center" prop="studentNo" min-width="130" />
      <el-table-column label="姓名" align="center" prop="studentName" min-width="100" />
      <el-table-column label="照片" align="center" prop="photo" width="90">
        <template #default="scope">
          <image-preview v-if="scope.row.photo" :src="scope.row.photo" :width="42" :height="42" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="身份证号" align="center" prop="idCard" min-width="180" />
      <el-table-column label="年龄" align="center" prop="age" width="80" />
      <el-table-column label="性别" align="center" prop="gender" width="90">
        <template #default="scope">
          <dict-tag :options="sys_user_sex" :value="scope.row.gender" />
        </template>
      </el-table-column>
      <el-table-column label="年级" align="center" prop="grade" min-width="110" />
      <el-table-column label="班级" align="center" prop="className" min-width="120" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="140">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:student:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:student:remove']">删除</el-button>
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

    <!-- 新增/修改学生对话框 -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="studentRef" :model="form" :rules="rules" label-width="92px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" placeholder="请输入学号" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="studentName">
              <el-input v-model="form.studentName" placeholder="请输入姓名" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="照片" prop="photo">
              <image-upload v-model="form.photo" :limit="1" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="1" :max="150" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option
                  v-for="dict in sys_user_sex"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年级" prop="grade">
              <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%" @change="handleFormGradeChange">
                <el-option
                  v-for="item in gradeOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="班级" prop="classId">
              <el-select v-model="form.classId" placeholder="请先选择年级" style="width: 100%">
                <el-option
                  v-for="item in formClassOptions"
                  :key="item.classId"
                  :label="item.className"
                  :value="item.classId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" maxlength="200" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量调班对话框 -->
    <el-dialog title="批量调班" v-model="transferOpen" width="520px" append-to-body @close="cancelTransfer">
      <div class="tip-text">
        已选择 {{ transferForm.studentIds.length }} 名学生，提交后将统一更新他们的班级。
      </div>
      <el-form ref="transferRef" :model="transferForm" :rules="transferRules" label-width="92px">
        <el-form-item label="目标年级" prop="grade">
          <el-select v-model="transferForm.grade" placeholder="请选择目标年级" style="width: 100%" @change="handleTransferGradeChange">
            <el-option
              v-for="item in gradeOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标班级" prop="classId">
          <el-select v-model="transferForm.classId" placeholder="请先选择目标年级" style="width: 100%">
            <el-option
              v-for="item in transferClassOptions"
              :key="item.classId"
              :label="item.className"
              :value="item.classId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="调班备注" prop="remark">
          <el-input
            v-model="transferForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入调班备注，可选"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitTransferForm">确 定</el-button>
          <el-button @click="cancelTransfer">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <excel-import-dialog
      ref="importStudentRef"
      title="学生导入"
      action="/system/student/importData"
      template-action="/system/student/importTemplate"
      template-file-name="student_template"
      update-support-label="是否更新已经存在的学生数据"
      @success="getList"
    />

    <!-- 班级统计对话框 -->
    <el-dialog title="班级统计" v-model="classStatsOpen" width="780px" append-to-body>
      <el-table v-loading="classStatsLoading" :data="classStatsList">
        <el-table-column label="年级" align="center" prop="grade" />
        <el-table-column label="班级" align="center" prop="className" />
        <el-table-column label="学生人数" align="center" prop="studentCount" />
        <el-table-column label="男生人数" align="center" prop="maleCount" />
        <el-table-column label="女生人数" align="center" prop="femaleCount" />
        <el-table-column label="未知人数" align="center" prop="unknownCount" />
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="classStatsOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Student">
import ExcelImportDialog from "@/components/ExcelImportDialog"
import { listStudent, getStudent, delStudent, addStudent, updateStudent, listStudentClassStats, transferStudentClass } from "@/api/system/student"
import { optionselectClass } from "@/api/system/class"

const { proxy } = getCurrentInstance()
const { sys_user_sex } = useDict("sys_user_sex")

const studentList = ref([])
const open = ref(false)
const transferOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const classStatsOpen = ref(false)
const classStatsLoading = ref(false)
const classStatsList = ref([])

// 班级下拉选项
const classOptions = ref([])
const formClassOptions = ref([])
const transferClassOptions = ref([])
const gradeOptions = ref([])

const data = reactive({
  form: {},
  transferForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentNo: undefined,
    studentName: undefined,
    gender: undefined,
    grade: undefined,
    classId: undefined
  },
  rules: {
    studentNo: [{ required: true, message: "学号不能为空", trigger: "blur" }],
    studentName: [{ required: true, message: "姓名不能为空", trigger: "blur" }],
    idCard: [
      { required: true, message: "身份证号不能为空", trigger: "blur" },
      { pattern: /(^\d{15}$)|(^\d{17}([0-9Xx])$)/, message: "身份证号格式不正确", trigger: "blur" }
    ],
    age: [
      { required: true, message: "年龄不能为空", trigger: "blur" },
      { type: "number", min: 1, max: 150, message: "年龄必须在 1 到 150 之间", trigger: "blur" }
    ],
    gender: [{ required: true, message: "性别不能为空", trigger: "change" }],
    grade: [{ required: true, message: "年级不能为空", trigger: "change" }],
    classId: [{ required: true, message: "班级不能为空", trigger: "change" }]
  },
  transferRules: {
    grade: [{ required: true, message: "目标年级不能为空", trigger: "change" }],
    classId: [{ required: true, message: "目标班级不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules, transferForm, transferRules } = toRefs(data)

/** 加载年级选项（从全部班级列表中提取去重） */
function loadGradeOptions() {
  optionselectClass().then(response => {
    const allClasses = response.data || []
    gradeOptions.value = [...new Set(allClasses.map(item => item.grade).filter(Boolean))].sort()
  })
}

/** 搜索栏年级变化时，加载对应班级 */
function handleGradeChange(grade) {
  queryParams.value.classId = undefined
  if (grade) {
    optionselectClass(grade).then(response => {
      classOptions.value = response.data || []
    })
  } else {
    classOptions.value = []
  }
}

/** 表单年级变化时，加载对应班级 */
function handleFormGradeChange(grade) {
  form.value.classId = undefined
  if (grade) {
    optionselectClass(grade).then(response => {
      formClassOptions.value = response.data || []
    })
  } else {
    formClassOptions.value = []
  }
}

/** 调班年级变化时，加载对应班级 */
function handleTransferGradeChange(grade) {
  transferForm.value.classId = undefined
  if (grade) {
    optionselectClass(grade).then(response => {
      transferClassOptions.value = response.data || []
    })
  } else {
    transferClassOptions.value = []
  }
}

function getList() {
  loading.value = true
  listStudent(queryParams.value).then(response => {
    studentList.value = response.rows
    total.value = response.total
  }).finally(() => {
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    studentId: undefined,
    studentNo: undefined,
    studentName: undefined,
    idCard: undefined,
    age: undefined,
    gender: undefined,
    grade: undefined,
    classId: undefined,
    photo: undefined,
    remark: undefined
  }
  formClassOptions.value = []
  proxy.resetForm("studentRef")
}

function resetTransfer() {
  transferForm.value = {
    studentIds: [],
    grade: undefined,
    classId: undefined,
    remark: undefined
  }
  transferClassOptions.value = []
  proxy.resetForm("transferRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  classOptions.value = []
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.studentId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加学生信息"
}

function handleImport() {
  proxy.$refs["importStudentRef"].open()
}

function handleClassStats() {
  classStatsOpen.value = true
  classStatsLoading.value = true
  listStudentClassStats().then(response => {
    classStatsList.value = response.data || []
  }).finally(() => {
    classStatsLoading.value = false
  })
}

function handleTransferClass() {
  if (!ids.value.length) {
    proxy.$modal.msgWarning("请先选择需要调班的学生")
    return
  }
  resetTransfer()
  transferForm.value.studentIds = [...ids.value]
  transferOpen.value = true
}

function cancelTransfer() {
  transferOpen.value = false
  resetTransfer()
}

function goTransferRecord() {
  proxy.$router.push("/system/student/transferRecord")
}

function handleUpdate(row) {
  reset()
  const studentId = row.studentId || ids.value[0]
  getStudent(studentId).then(response => {
    form.value = response.data
    // 回显时根据年级加载班级列表
    if (form.value.grade) {
      optionselectClass(form.value.grade).then(res => {
        formClassOptions.value = res.data || []
        open.value = true
        title.value = "修改学生信息"
      })
    } else {
      open.value = true
      title.value = "修改学生信息"
    }
  })
}

function submitForm() {
  proxy.$refs["studentRef"].validate(valid => {
    if (valid) {
      if (form.value.studentId !== undefined) {
        updateStudent(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addStudent(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function submitTransferForm() {
  proxy.$refs["transferRef"].validate(valid => {
    if (valid) {
      transferForm.value.studentIds = [...ids.value]
      transferStudentClass(transferForm.value).then(() => {
        proxy.$modal.msgSuccess("批量调班成功")
        transferOpen.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const studentIds = row.studentId || ids.value
  proxy.$modal.confirm('是否确认删除学生编号为"' + studentIds + '"的数据项？').then(function () {
    return delStudent(studentIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("system/student/export", {
    ...queryParams.value
  }, `student_${new Date().getTime()}.xlsx`)
}

function handleExportSelected() {
  if (!ids.value.length) {
    proxy.$modal.msgWarning("请先选择要导出的学生")
    return
  }
  proxy.download("system/student/export", {
    studentIds: ids.value.join(",")
  }, `student_selected_${new Date().getTime()}.xlsx`)
}

// 初始化：加载年级选项 + 学生列表
loadGradeOptions()
getList()
</script>

<style scoped>
.tip-text {
  margin-bottom: 16px;
  color: #606266;
}
</style>
