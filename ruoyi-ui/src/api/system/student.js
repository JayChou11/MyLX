import request from '@/utils/request'

// 查询学生信息列表
export function listStudent(query) {
  return request({
    url: '/system/student/list',
    method: 'get',
    params: query
  })
}

// 查询学生信息详细
export function getStudent(studentId) {
  return request({
    url: '/system/student/' + studentId,
    method: 'get'
  })
}

// 查询学生完整档案
export function getStudentProfile(studentId) {
  return request({
    url: '/system/student/profile/' + studentId,
    method: 'get'
  })
}

// 查询学生班级统计
export function listStudentClassStats() {
  return request({
    url: '/system/student/classStats',
    method: 'get'
  })
}

// 刷新班级统计缓存
export function refreshClassStatsCache() {
  return request({
    url: '/system/student/classStats/cache',
    method: 'delete'
  })
}

// 新增学生信息
export function addStudent(data) {
  return request({
    url: '/system/student',
    method: 'post',
    data: data
  })
}

// 修改学生信息
export function updateStudent(data) {
  return request({
    url: '/system/student',
    method: 'put',
    data: data
  })
}

// 批量调班
export function transferStudentClass(data) {
  return request({
    url: '/system/student/transferClass',
    method: 'put',
    data: data
  })
}

// 删除学生信息
export function delStudent(studentId) {
  return request({
    url: '/system/student/' + studentId,
    method: 'delete'
  })
}

// 执行升年级操作
export function upgradeGrade() {
  return request({
    url: '/system/student/upgradeGrade',
    method: 'post'
  })
}
