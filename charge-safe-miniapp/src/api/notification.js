import request from './request'

export function pageMyNotifications(params) {
  return request({ url: '/api/notifications', method: 'GET', data: params })
}

export function markNotificationRead(id) {
  return request({ url: `/api/notifications/${id}/read`, method: 'POST' })
}

export function markAllNotificationsRead() {
  return request({ url: '/api/notifications/read-all', method: 'POST' })
}

export function getUnreadNotificationCount() {
  return request({ url: '/api/notifications/unread-count', method: 'GET' })
}
