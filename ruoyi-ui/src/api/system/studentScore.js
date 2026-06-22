import request from '@/utils/request'

// 查询学生成绩列表
// query 会作为 URL 查询参数传给后端，例如：
// /system/studentScore/list?pageNum=1&pageSize=10&studentNo=2026001
export function listStudentScore(query) {
  return request({
    url: '/system/studentScore/list',
    method: 'get',
    params: query
  })
}

// 查询学生成绩详细
// scoreId 拼在路径里，对应后端 @GetMapping("/{scoreId}") 和 @PathVariable("scoreId")。
export function getStudentScore(scoreId) {
  return request({
    url: '/system/studentScore/' + scoreId,
    method: 'get'
  })
}

// 查询班级成绩统计
// 统计接口复用当前查询条件，例如按某个考试名称、年级、班级统计。
export function listStudentScoreClassStats(query) {
  return request({
    url: '/system/studentScore/classStats',
    method: 'get',
    params: query
  })
}

// 新增学生成绩
// data 放在请求体里，对应后端 @RequestBody SysStudentScore。
export function addStudentScore(data) {
  return request({
    url: '/system/studentScore',
    method: 'post',
    data: data
  })
}

// 修改学生成绩
// 修改时 data 中必须带 scoreId，后端根据 scoreId 更新对应记录。
export function updateStudentScore(data) {
  return request({
    url: '/system/studentScore',
    method: 'put',
    data: data
  })
}

// 删除学生成绩
// scoreId 可以是单个 ID，也可以是逗号拼接的多个 ID，例如 "1,2,3"。
export function delStudentScore(scoreId) {
  return request({
    url: '/system/studentScore/' + scoreId,
    method: 'delete'
  })
}
