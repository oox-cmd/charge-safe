<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo" @click="router.push('/')">
        <el-icon :size="24"><Lightning /></el-icon>
        <span v-show="!isCollapse">ChargeSafe</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="side-menu"
      >
        <template v-for="item in menuItems" :key="item.path">
          <el-menu-item :index="item.path">
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
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <el-icon :size="20" class="notification-icon" @click="router.push('/notification')">
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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/api/notification'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const unreadCount = ref(0)

// 根据角色过滤菜单
const allMenuItems = [
  { path: '/dashboard', title: '工作台', icon: 'Odometer', roles: [] },
  { path: '/community', title: '社区管理', icon: 'OfficeBuilding', roles: ['PLATFORM_ADMIN'] },
  { path: '/parking-area', title: '车棚管理', icon: 'Garage', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] },
  { path: '/device', title: '设备管理', icon: 'Monitor', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] },
  { path: '/port', title: '端口管理', icon: 'Connection', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] },
  { path: '/reservation', title: '预约记录', icon: 'Clock', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] },
  { path: '/order', title: '充电订单', icon: 'Document', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] },
  { path: '/repair-report', title: '报修记录', icon: 'Warning', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN'] },
  { path: '/repair-ticket', title: '维修工单', icon: 'Tickets', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'MAINTAINER'] },
  { path: '/inspection-task', title: '巡检任务', icon: 'Checked', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'INSPECTOR'] },
  { path: '/hazard', title: '安全隐患', icon: 'WarnTriangleFilled', roles: ['PLATFORM_ADMIN', 'PROPERTY_ADMIN', 'INSPECTOR'] },
  { path: '/notification', title: '消息通知', icon: 'Bell', roles: [] }
]

const menuItems = computed(() => {
  return allMenuItems.filter(item => {
    if (!item.roles || item.roles.length === 0) return true
    return item.roles.includes(userStore.roleCode)
  })
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
  // 每分钟刷新未读消息数
  setInterval(fetchUnreadCount, 60000)
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
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  gap: 8px;
  background: #2b3648;
}

.side-menu {
  border-right: none;
  background: #304156;
}

.side-menu .el-menu-item {
  color: #bfcbd9;
}

.side-menu .el-menu-item:hover,
.side-menu .el-menu-item.is-active {
  background: #263445;
  color: #409eff;
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
}
</style>
