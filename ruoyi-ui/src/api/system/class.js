import request from '@/utils/request'

// 查询班级信息列表
export function listClass(query) {
  return request({
    url: '/system/class/list',
    method: 'get',
    params: query
  })
}

// 查询班级选项列表（不分页，用于下拉选择）
export function optionselectClass(grade) {
  return request({
    url: '/system/class/optionselect',
    method: 'get',
    params: { grade }
  })
}

// 查询班级信息详细
export function getClass(classId) {
  return request({
    url: '/system/class/' + classId,
    method: 'get'
  })
}

// 新增班级信息
export function addClass(data) {
  return request({
    url: '/system/class',
    method: 'post',
    data: data
  })
}

// 修改班级信息
export function updateClass(data) {
  return request({
    url: '/system/class',
    method: 'put',
    data: data
  })
}

// 删除班级信息
export function delClass(classId) {
  return request({
    url: '/system/class/' + classId,
    method: 'delete'
  })
}
