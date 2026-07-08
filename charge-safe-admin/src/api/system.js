import request from './request'

// ==================== 用户管理 ====================

export function getUsers(params) {
  return request.get('/api/users', { params })
}

export function getUser(id) {
  return request.get(`/api/users/${id}`)
}

export function createUser(data) {
  return request.post('/api/users', data)
}

export function updateUser(id, data) {
  return request.put(`/api/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/api/users/${id}`)
}

export function updateUserStatus(id, status) {
  return request.put(`/api/users/${id}/status`, null, { params: { status } })
}

export function assignUserRoles(id, roleIds) {
  return request.put(`/api/users/${id}/roles`, { roleIds })
}

export function resetUserPassword(id) {
  return request.post(`/api/users/${id}/reset-password`)
}

// ==================== 角色管理 ====================

export function getRoles(params) {
  return request.get('/api/roles', { params })
}

export function getRoleList() {
  return request.get('/api/roles/list')
}

export function getRole(id) {
  return request.get(`/api/roles/${id}`)
}

export function createRole(data) {
  return request.post('/api/roles', data)
}

export function updateRole(id, data) {
  return request.put(`/api/roles/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/api/roles/${id}`)
}

export function assignRoleMenus(id, menuIds) {
  return request.put(`/api/roles/${id}/menus`, { menuIds })
}

// ==================== 菜单管理 ====================

export function getMenus(params) {
  return request.get('/api/menus', { params })
}

export function getMenuTree() {
  return request.get('/api/menus/tree')
}

export function getMenu(id) {
  return request.get(`/api/menus/${id}`)
}

export function createMenu(data) {
  return request.post('/api/menus', data)
}

export function updateMenu(id, data) {
  return request.put(`/api/menus/${id}`, data)
}

export function deleteMenu(id) {
  return request.delete(`/api/menus/${id}`)
}
