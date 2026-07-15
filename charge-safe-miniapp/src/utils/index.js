/**
 * 工具函数
 */
/** 页面跳转 */
export function navigateTo(url, params = {}) {
  const query = Object.keys(params).map(k => `${k}=${params[k]}`).join('&')
  uni.navigateTo({ url: query ? `${url}?${query}` : url })
}

/** 关闭当前页跳转 */
export function redirectTo(url, params = {}) {
  const query = Object.keys(params).map(k => `${k}=${params[k]}`).join('&')
  uni.redirectTo({ url: query ? `${url}?${query}` : url })
}

/** 跳转 tab 页 */
export function switchTab(url) {
  uni.switchTab({ url })
}

export function navigateBack(delta = 1) {
  uni.navigateBack({ delta })
}

export function showLoading(title = '加载中...') {
  uni.showLoading({ title, mask: true })
}

export function hideLoading() {
  uni.hideLoading()
}

export function showToast(title, icon = 'none') {
  uni.showToast({ title, icon, duration: 2000 })
}

/** 手机号校验 */
export function isValidPhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone)
}

/** 密码校验 */
export function isValidPassword(password) {
  return password && password.length >= 6
}

/** 脱敏手机号 */
export function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || ''
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}
