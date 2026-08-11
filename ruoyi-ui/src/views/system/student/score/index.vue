<template>
  <div class="app-container">
    <!--
      查询表单：
      v-model 绑定 queryParams 中的字段，点击搜索时会作为 GET 参数传给后端 list 接口。
      这里的“年级 -> 班级”是联动下拉，先选年级，再加载该年级下的班级。
    -->
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
      <el-form-item label="考试" prop="examName">
        <el-input
          v-model="queryParams.examName"
          placeholder="请输入考试名称"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="年级" prop="grade">
        <el-select v-model="queryParams.grade" placeholder="请选择年级" clearable style="width: 180px" @change="handleGradeChange">
          <el-option
            v-for="item in gradeOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="班级" prop="classId">
        <el-select v-model="queryParams.classId" placeholder="请选择班级" clearable style="width: 180px">
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

    <!--
      工具栏按钮：
      v-hasPermi 会根据当前用户拥有的权限编码决定按钮是否显示。
      这些权限编码需要和 SQL 脚本里 sys_menu.perms 保持一致。
    -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:studentScore:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['system:studentScore:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:studentScore:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:studentScore:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExportSelected" v-hasPermi="['system:studentScore:export']">选择导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['system:studentScore:import']">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="DataAnalysis" @click="handleClassStats" v-hasPermi="['system:studentScore:stat']">班级成绩统计</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="TrendCharts" :disabled="single" @click="handleTrend" v-hasPermi="['system:studentScore:query']">成绩分析</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="WarningFilled" @click="handleWarning" v-hasPermi="['system:studentScore:query']">成绩预警</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!--
      成绩列表：
      后端返回的是 TableDataInfo，rows 对应 scoreList，total 对应分页总数。
      总分、平均分不是前端算出来的，而是后端 Service 统一计算后返回。
    -->
    <el-table v-loading="loading" :data="scoreList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="成绩ID" align="center" prop="scoreId" width="90" />
      <el-table-column label="学号" align="center" prop="studentNo" min-width="130" />
      <el-table-column label="姓名" align="center" prop="studentName" min-width="100" />
      <el-table-column label="年级" align="center" prop="grade" min-width="100" />
      <el-table-column label="班级" align="center" prop="className" min-width="110" />
      <el-table-column label="考试名称" align="center" prop="examName" min-width="150" show-overflow-tooltip />
      <el-table-column label="学期" align="center" prop="semester" min-width="100" />
      <el-table-column label="考试日期" align="center" prop="examDate" width="120">
        <template #default="scope">
          <span>{{ parseTime(scope.row.examDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="语文" align="center" prop="chineseScore" width="90" />
      <el-table-column label="数学" align="center" prop="mathScore" width="90" />
      <el-table-column label="英语" align="center" prop="englishScore" width="90" />
      <el-table-column label="总分" align="center" prop="totalScore" width="90" />
      <el-table-column label="平均分" align="center" prop="averageScore" width="90" />
      <el-table-column label="班级排名" align="center" prop="classRank" width="100" />
      <el-table-column label="年级排名" align="center" prop="gradeRank" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="210">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:studentScore:edit']">修改</el-button>
          <el-button link type="primary" icon="TrendCharts" @click="handleTrend(scope.row)" v-hasPermi="['system:studentScore:query']">分析</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:studentScore:remove']">删除</el-button>
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

    <!-- 新增/修改成绩弹窗：新增和修改共用同一个 form，通过 form.scoreId 是否存在区分。 -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="scoreRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="学生" prop="studentId">
          <!--
            学生下拉框展示“学号 - 姓名（年级班级）”，但真正提交的是 studentId。
            这样数据库能稳定用 student_id 建关联，不受姓名重复或改名影响。
          -->
          <el-select v-model="form.studentId" filterable placeholder="请选择学生" style="width: 100%" @change="handleFormStudentChange">
            <el-option
              v-for="item in studentOptions"
              :key="item.studentId"
              :label="item.studentNo + ' - ' + item.studentName + '（' + item.grade + item.className + '）'"
              :value="item.studentId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考试批次" prop="examId">
          <el-select v-model="form.examId" filterable placeholder="请选择考试批次" style="width: 100%" @change="handleExamChange">
            <el-option
              v-for="item in filteredExamOptions"
              :key="item.examId"
              :label="formatExamOption(item)"
              :value="item.examId"
            />
          </el-select>
        </el-form-item>
        <el-row>
          <el-col :span="8">
            <el-form-item label="语文" prop="chineseScore">
              <el-input-number v-model="form.chineseScore" :min="0" :max="100" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="数学" prop="mathScore">
              <el-input-number v-model="form.mathScore" :min="0" :max="100" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="英语" prop="englishScore">
              <el-input-number v-model="form.englishScore" :min="0" :max="100" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注，可选" maxlength="500" show-word-limit />
        </el-form-item>
        <div class="score-tip">
          总分和平均分由后端根据三科成绩自动计算，前端不需要手填。
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!--
      通用 Excel 导入组件：
      action 是上传接口，template-action 是模板下载接口。
      上传成功后触发 success 事件，重新查询列表。
    -->
    <excel-import-dialog
      ref="importScoreRef"
      title="成绩导入"
      action="/system/studentScore/importData"
      template-action="/system/studentScore/importTemplate"
      template-file-name="student_score_template"
      update-support-label="是否更新已经存在的成绩数据"
      @success="getList"
    />

    <!--
      班级成绩统计弹窗：
      数据来自 /system/studentScore/classStats。
      后端 SQL 按“班级 + 考试名称” group by，前端只负责展示统计结果。
    -->
    <el-dialog title="班级成绩统计" v-model="classStatsOpen" width="1080px" append-to-body>
      <el-table v-loading="classStatsLoading" :data="classStatsList">
        <el-table-column label="年级" align="center" prop="grade" />
        <el-table-column label="班级" align="center" prop="className" />
        <el-table-column label="考试名称" align="center" prop="examName" min-width="150" show-overflow-tooltip />
        <el-table-column label="人数" align="center" prop="studentCount" />
        <el-table-column label="语文均分" align="center" prop="avgChineseScore" />
        <el-table-column label="数学均分" align="center" prop="avgMathScore" />
        <el-table-column label="英语均分" align="center" prop="avgEnglishScore" />
        <el-table-column label="班级均分" align="center" prop="avgScore" />
        <el-table-column label="最高总分" align="center" prop="maxTotalScore" />
        <el-table-column label="最低总分" align="center" prop="minTotalScore" />
        <el-table-column label="及格人数" align="center" prop="passCount" />
        <el-table-column label="优秀人数" align="center" prop="excellentCount" />
        <el-table-column label="不及格人数" align="center" prop="failCount" />
      </el-table>
      <template #footer>
        <el-button @click="classStatsOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!--
      成绩分析弹窗：
      上方用 ECharts 展示总分/平均分趋势；
      下方用表格展示每次考试的班级排名和年级排名。
    -->
    <el-dialog title="成绩分析" v-model="trendOpen" width="980px" append-to-body @closed="destroyTrendChart">
      <div v-loading="trendLoading">
        <el-descriptions :column="4" border class="trend-summary">
          <el-descriptions-item label="学号">{{ trendStudent.studentNo || "-" }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ trendStudent.studentName || "-" }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ trendStudent.grade || "-" }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ trendStudent.className || "-" }}</el-descriptions-item>
        </el-descriptions>
        <div ref="trendChartRef" class="trend-chart"></div>
        <el-table :data="trendList" size="small">
          <el-table-column label="考试名称" align="center" prop="examName" min-width="160" show-overflow-tooltip />
          <el-table-column label="总分" align="center" prop="totalScore" />
          <el-table-column label="平均分" align="center" prop="averageScore" />
          <el-table-column label="班级排名" align="center" prop="classRank" />
          <el-table-column label="年级排名" align="center" prop="gradeRank" />
          <el-table-column label="录入时间" align="center" prop="createTime" width="170">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="trendOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <el-dialog title="成绩预警" v-model="warningOpen" width="1180px" append-to-body>
      <div v-loading="warningLoading">
        <el-table :data="warningList" size="small" border>
          <el-table-column label="学号" align="center" prop="studentNo" width="120" />
          <el-table-column label="姓名" align="center" prop="studentName" width="90" />
          <el-table-column label="年级" align="center" prop="grade" width="90" />
          <el-table-column label="班级" align="center" prop="className" width="110" />
          <el-table-column label="考试名称" align="center" prop="examName" min-width="150" show-overflow-tooltip />
          <el-table-column label="总分" align="center" prop="totalScore" width="90" />
          <el-table-column label="班级排名" align="center" prop="classRank" width="95" />
          <el-table-column label="年级排名" align="center" prop="gradeRank" width="95" />
          <el-table-column label="预警类型" align="center" prop="warningTypes" min-width="160" show-overflow-tooltip />
          <el-table-column label="预警原因" align="center" prop="warningReason" min-width="280" show-overflow-tooltip />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="warningOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="StudentScore">
import * as echarts from "echarts"
import ExcelImportDialog from "@/components/ExcelImportDialog"
import { listStudent } from "@/api/system/student"
import { optionselectClass } from "@/api/system/class"
import { listExam } from "@/api/system/exam"
import { parseTime } from "@/utils/ruoyi"
import { listStudentScore, getStudentScore, delStudentScore, addStudentScore, updateStudentScore, listStudentScoreClassStats, listStudentScoreTrend, listStudentScoreWarning } from "@/api/system/studentScore"

const { proxy } = getCurrentInstance()

// 列表数据：表格展示的每一行学生成绩。
const scoreList = ref([])
// 学生下拉选项：新增/修改成绩时选择学生使用。
const studentOptions = ref([])
// 考试批次下拉选项：新增/修改成绩时提交 examId，考试名称只作为展示和兼容字段。
const examOptions = ref([])
// 当前查询年级下的班级下拉选项。
const classOptions = ref([])
// 年级下拉选项，从班级列表中提取并去重。
const gradeOptions = ref([])
// 新增/修改弹窗是否显示。
const open = ref(false)
// 表格 loading 状态，避免请求过程中页面看起来像“没反应”。
const loading = ref(true)
// 是否显示查询表单，由 right-toolbar 控制。
const showSearch = ref(true)
// 当前表格勾选的成绩 ID 集合。
const ids = ref([])
// 是否只选中一行；修改按钮需要单选，所以用它控制禁用状态。
const single = ref(true)
// 是否没有选中任何行；删除、选择导出需要至少选一行。
const multiple = ref(true)
// 分页总条数，来自后端 TableDataInfo.total。
const total = ref(0)
// 弹窗标题：添加学生成绩 / 修改学生成绩。
const title = ref("")
// 班级成绩统计弹窗状态和数据。
const classStatsOpen = ref(false)
const classStatsLoading = ref(false)
const classStatsList = ref([])
const warningOpen = ref(false)
const warningLoading = ref(false)
const warningList = ref([])
// 成绩趋势分析弹窗状态。
// trendOpen 控制弹窗显示隐藏；trendLoading 控制接口加载时的遮罩；
// trendList 保存后端返回的多次考试记录；trendStudent 保存当前正在分析的学生基础信息。
const trendOpen = ref(false)
const trendLoading = ref(false)
const trendList = ref([])
const trendStudent = ref({})
// ECharts 初始化时需要拿到真实 DOM，所以这里用 ref 绑定图表容器 div。
const trendChartRef = ref(null)
// 图表实例不能放到 ref 里参与页面渲染，它只是第三方库对象；用普通变量保存即可。
let trendChartInstance = null

const data = reactive({
  // 新增/修改表单对象。
  form: {},
  // 查询参数对象。字段名要和后端 SysStudentScore 属性名保持一致，Spring 才能自动封装。
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentNo: undefined,
    studentName: undefined,
    examName: undefined,
    grade: undefined,
    classId: undefined
  },
  // 前端表单校验。注意：后端实体上也有校验，前端校验只是提前拦截，不能替代后端校验。
  rules: {
    studentId: [{ required: true, message: "学生不能为空", trigger: "change" }],
    examId: [{ required: true, message: "考试批次不能为空", trigger: "change" }],
    chineseScore: [{ required: true, message: "语文成绩不能为空", trigger: "blur" }],
    mathScore: [{ required: true, message: "数学成绩不能为空", trigger: "blur" }],
    englishScore: [{ required: true, message: "英语成绩不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const filteredExamOptions = computed(() => {
  const student = studentOptions.value.find(item => item.studentId === form.value.studentId)
  if (!student || !student.grade) {
    return examOptions.value
  }
  return examOptions.value.filter(item => item.grade === student.grade)
})

/** 查询成绩列表 */
function getList() {
  loading.value = true
  listStudentScore(queryParams.value).then(response => {
    scoreList.value = response.rows
    total.value = response.total
  }).finally(() => {
    loading.value = false
  })
}

/** 加载学生下拉选项 */
function loadStudentOptions() {
  // 这里复用学生列表接口。用于学习阶段数据量不大可以这样做；真实大数据量项目通常会做远程搜索。
  listStudent({}).then(response => {
    studentOptions.value = response.rows || []
  })
}

/** 加载考试批次下拉选项 */
function loadExamOptions() {
  listExam({ pageNum: 1, pageSize: 1000, examStatus: "0" }).then(response => {
    examOptions.value = response.rows || []
  })
}

/** 加载年级选项 */
function loadGradeOptions() {
  optionselectClass().then(response => {
    const allClasses = response.data || []
    // Set 用来去重：多个班级可能属于同一个年级，年级下拉只需要显示一次。
    gradeOptions.value = [...new Set(allClasses.map(item => item.grade).filter(Boolean))].sort()
  })
}

/** 年级变化时，清空旧班级并重新加载该年级下的班级 */
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

/** 搜索：回到第一页，再按最新条件查询 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置查询条件 */
function resetQuery() {
  proxy.resetForm("queryRef")
  classOptions.value = []
  handleQuery()
}

/** 表格选择变化：维护已选 ID，以及按钮禁用状态 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.scoreId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 重置新增/修改表单 */
function reset() {
  form.value = {
    scoreId: undefined,
    studentId: undefined,
    examId: undefined,
    examName: undefined,
    chineseScore: undefined,
    mathScore: undefined,
    englishScore: undefined,
    remark: undefined
  }
}

function formatExamOption(item) {
  const examDate = item.examDate ? parseTime(item.examDate, "{y}-{m}-{d}") : "-"
  return `${item.examName} - ${item.grade} - ${item.semester} - ${examDate}`
}

function handleExamChange(examId) {
  const exam = examOptions.value.find(item => item.examId === examId)
  form.value.examName = exam ? exam.examName : undefined
}

function handleFormStudentChange() {
  if (!form.value.examId) {
    return
  }
  const selectedExam = examOptions.value.find(item => item.examId === form.value.examId)
  const selectedStudent = studentOptions.value.find(item => item.studentId === form.value.studentId)
  if (selectedExam && selectedStudent && selectedStudent.grade && selectedExam.grade !== selectedStudent.grade) {
    form.value.examId = undefined
    form.value.examName = undefined
  }
}

/** 打开新增弹窗 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加学生成绩"
  nextTick(() => {
    proxy.resetForm("scoreRef")
  })
}

/** 打开修改弹窗 */
function handleUpdate(row) {
  reset()
  // 行内“修改”会传 row；工具栏“修改”没有 row，就使用当前勾选的第一条 ID。
  const scoreId = row.scoreId || ids.value[0]
  getStudentScore(scoreId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改学生成绩"
    nextTick(() => {
      proxy.resetForm("scoreRef")
    })
  })
}

/** 提交新增/修改表单 */
function submitForm() {
  proxy.$refs["scoreRef"].validate(valid => {
    if (valid) {
      // 有 scoreId 表示修改；没有 scoreId 表示新增。
      if (form.value.scoreId !== undefined) {
        updateStudentScore(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addStudentScore(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 关闭弹窗并清空表单 */
function cancel() {
  open.value = false
  reset()
}

/** 删除成绩，支持单条删除和批量删除 */
function handleDelete(row) {
  // 行内删除时 row.scoreId 有值；批量删除时使用 ids.value 数组。
  const scoreIds = row.scoreId || ids.value
  proxy.$modal.confirm('是否确认删除成绩编号为"' + scoreIds + '"的数据项？').then(function () {
    return delStudentScore(scoreIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 按当前查询条件导出 */
function handleExport() {
  proxy.download("system/studentScore/export", {
    ...queryParams.value
  }, `student_score_${new Date().getTime()}.xlsx`)
}

/** 只导出当前勾选的成绩 */
function handleExportSelected() {
  if (!ids.value.length) {
    proxy.$modal.msgWarning("请先选择要导出的成绩")
    return
  }
  proxy.download("system/studentScore/export", {
    // 后端接收的是 scoreIds 字符串，再用 Convert.toLongArray 转成 Long[]。
    scoreIds: ids.value.join(",")
  }, `student_score_selected_${new Date().getTime()}.xlsx`)
}

/** 打开导入弹窗 */
function handleImport() {
  proxy.$refs["importScoreRef"].open()
}

/** 打开成绩预警弹窗 */
function handleWarning() {
  warningOpen.value = true
  warningLoading.value = true
  warningList.value = []
  // 预警接口复用当前查询条件，这样页面上筛选出来的成绩范围和预警结果保持一致。
  listStudentScoreWarning(queryParams.value).then(response => {
    warningList.value = response.data || []
  }).finally(() => {
    warningLoading.value = false
  })
}

/** 查询并打开班级成绩统计弹窗 */
function handleClassStats() {
  classStatsOpen.value = true
  classStatsLoading.value = true
  // 统计接口复用当前查询条件，例如只统计某个考试名称或某个年级。
  listStudentScoreClassStats(queryParams.value).then(response => {
    classStatsList.value = response.data || []
  }).finally(() => {
    classStatsLoading.value = false
  })
}

/**
 * 打开成绩分析弹窗。
 *
 * 这个方法有两个入口：
 * 1. 行内“分析”按钮：会把当前行 row 传进来；
 * 2. 工具栏“成绩分析”按钮：不会传 row，需要从勾选的 ids 中找到当前选中的那条成绩。
 *
 * 找到成绩行以后，只拿 studentId 调后端趋势接口。
 * 因为趋势分析关注的是“这个学生所有考试的变化”，不是只看当前这一条 scoreId。
 */
function handleTrend(row) {
  // 行内“分析”会传 row；工具栏“成绩分析”没有 row，就使用当前勾选的那条成绩。
  const current = row && row.studentId ? row : scoreList.value.find(item => item.scoreId === ids.value[0])
  if (!current) {
    proxy.$modal.msgWarning("请选择一条成绩记录")
    return
  }
  trendStudent.value = current
  trendOpen.value = true
  trendLoading.value = true
  listStudentScoreTrend(current.studentId).then(response => {
    // 后端 AjaxResult 的 data 就是趋势列表，每一项代表该学生的一次考试成绩。
    trendList.value = response.data || []
    // 等弹窗和图表容器渲染出来之后，再初始化 ECharts。
    // 如果不等 nextTick，trendChartRef.value 可能还是 null，图表就找不到挂载位置。
    nextTick(() => {
      renderTrendChart()
    })
  }).finally(() => {
    trendLoading.value = false
  })
}

/**
 * 渲染成绩趋势图。
 *
 * ECharts 的工作方式可以理解成三步：
 * 1. echarts.init(dom)：把一个普通 div 变成图表画布；
 * 2. 准备 xAxis/series 需要的数据数组；
 * 3. setOption(...)：把配置和数据交给 ECharts，让它完成绘制。
 */
function renderTrendChart() {
  if (!trendChartRef.value) {
    return
  }
  if (!trendChartInstance) {
    trendChartInstance = echarts.init(trendChartRef.value)
  }
  // x 轴展示考试名称；两条折线分别展示总分和平均分。
  // Number(...) 是为了避免后端 BigDecimal 序列化后被当成字符串，影响图表数值计算。
  const examNames = trendList.value.map(item => item.examName)
  const totalScores = trendList.value.map(item => Number(item.totalScore || 0))
  const averageScores = trendList.value.map(item => Number(item.averageScore || 0))
  trendChartInstance.setOption({
    tooltip: {
      trigger: "axis"
    },
    legend: {
      data: ["总分", "平均分"]
    },
    grid: {
      left: 40,
      right: 24,
      bottom: 40,
      top: 48,
      containLabel: true
    },
    xAxis: {
      type: "category",
      data: examNames,
      axisLabel: {
        interval: 0,
        // 考试次数较多时旋转文字，避免横轴名称挤在一起。
        rotate: examNames.length > 3 ? 25 : 0
      }
    },
    yAxis: {
      type: "value",
      min: 0
    },
    series: [
      {
        name: "总分",
        type: "line",
        smooth: true,
        data: totalScores
      },
      {
        name: "平均分",
        type: "line",
        smooth: true,
        data: averageScores
      }
    ]
  })
}

/**
 * 销毁图表实例。
 *
 * 弹窗关闭后 DOM 会被隐藏/销毁，如果 ECharts 实例一直留着，
 * 多次打开弹窗可能出现尺寸不准或内存占用增加的问题。
 * 所以关闭弹窗、离开页面时都主动 dispose。
 */
function destroyTrendChart() {
  if (trendChartInstance) {
    trendChartInstance.dispose()
    trendChartInstance = null
  }
}

onBeforeUnmount(() => {
  destroyTrendChart()
})

// 页面初始化：先准备下拉选项，再加载表格数据。
loadStudentOptions()
loadExamOptions()
loadGradeOptions()
getList()
</script>

<style scoped>
.score-tip {
  padding: 10px 12px;
  border-radius: 4px;
  color: #606266;
  background: #f5f7fa;
}

.trend-summary {
  margin-bottom: 16px;
}

.trend-chart {
  width: 100%;
  height: 320px;
  margin-bottom: 16px;
}
</style>
