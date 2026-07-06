<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo" @click="router.push('/ops')">
        <el-icon :size="24" class="logo-icon"><Lightning /></el-icon>
        <span v-show="!isCollapse" class="logo-text">物业运维端</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="side-menu"
      >
        <template v-for="item in menuItems" :key="item.path || item.title">
          <!-- 有子菜单 -->
          <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.title">
            <template #title>
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.title }}</span>
            </template>
            <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
              <el-icon><component :is="child.icon" /></el-icon>
              <template #title>{{ child.title }}</template>
            </el-menu-item>
          </el-sub-menu>
          <!-- 无子菜单 -->
          <el-menu-item v-else :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/ops' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <!-- 如果是平台管理员，显示切换至权限端按钮 -->
          <el-button v-if="userStore.roleCode === 'PLATFORM_ADMIN'" type="primary" size="small" plain icon="Switch" @click="router.push('/permission')">
            切换至权限管理端
          </el-button>
          
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <el-icon :size="20" class="notification-icon" @click="router.push('/ops/notification')">
              <Bell />
            </el-icon>
          </el-badge>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ userStore.nickname || userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <span>{{ userStore.roleName }}</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/api/notification'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const unreadCount = ref(0)
let unreadTimer = null

import { opsMenuItems } from '@/constants/menus'

// 根据角色动态过滤菜单
const menuItems = computed(() => {
  const role = userStore.roleCode
  return opsMenuItems.map(item => {
    // 如果有子项目，过滤子项目
    if (item.children && item.children.length > 0) {
      const filteredChildren = item.children.filter(child => {
        if (!child.roles || child.roles.length === 0) return true
        return child.roles.includes(role)
      })
      if (filteredChildren.length > 0) {
        return { ...item, children: filteredChildren }
      }
      return null
    }
    // 如果是叶子菜单项
    if (!item.roles || item.roles.length === 0) return item
    if (item.roles.includes(role)) return item
    return null
  }).filter(Boolean)
})

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取未读消息数失败:', error)
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}

onMounted(() => {
  fetchUnreadCount()
  unreadTimer = setInterval(fetchUnreadCount, 60000)
})

onUnmounted(() => {
  if (unreadTimer) {
    clearInterval(unreadTimer)
    unreadTimer = null
  }
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.aside {
  background: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  gap: 8px;
  background: #2b3648;
  border-bottom: 1px solid #3a4b5e;
}

.logo-icon {
  color: #67c23a;
}

.side-menu {
  border-right: none;
  background: #304156;
}

:deep(.el-menu) {
  background-color: #304156 !important;
  border: none;
}

:deep(.el-menu-item) {
  color: #bfcbd9 !important;
}

:deep(.el-sub-menu__title) {
  color: #bfcbd9 !important;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: #263445 !important;
  color: #fff !important;
}

:deep(.el-menu-item.is-active) {
  background-color: #409eff !important;
  color: #fff !important;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #666;
}

.collapse-btn:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notification-badge {
  cursor: pointer;
}

.notification-icon {
  color: #666;
  cursor: pointer;
}

.notification-icon:hover {
  color: #409eff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #666;
}

.username {
  font-size: 14px;
}

.main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
