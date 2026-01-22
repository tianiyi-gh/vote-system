<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">
        <h2>DZVOTE</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <el-sub-menu v-if="route.children && route.children.length > 1" :index="route.path">
            <template #title>
              <el-icon v-if="route.meta?.icon"><component :is="route.meta.icon" /></el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children.filter((c: any) => !c.meta?.hidden)"
              :key="child.path"
              :index="child.path === 'dashboard' ? '/' : route.path + '/' + child.path"
            >
              <el-icon v-if="child.meta?.icon"><component :is="child.meta.icon" /></el-icon>
              <span>{{ child.meta?.title }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item
            v-else-if="route.children && route.children.length === 1"
            :index="route.path"
          >
            <el-icon v-if="route.children[0].meta?.icon">
              <component :is="route.children[0].meta.icon" />
            </el-icon>
            <span>{{ route.children[0].meta?.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-content">
          <h3>大众投票系统管理后台</h3>
          <div class="user-info">
            <el-dropdown>
              <span class="el-dropdown-link">
                <el-icon><User /></el-icon>
                管理员
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>个人中心</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const menuRoutes = computed(() => {
  return router.getRoutes().filter(r => r.path === '/' || (r.path.startsWith('/') && r.path !== '/login'))
})

const activeMenu = computed(() => {
  return route.path
})

const handleLogout = () => {
  router.push('/login')
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
  
  .logo {
    height: 50px;
    line-height: 50px;
    text-align: center;
    color: #409eff;
    font-size: 20px;
    font-weight: bold;
    background-color: #2b3a4b;
    
    h2 {
      margin: 0;
    }
  }
}

.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  
  .header-content {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h3 {
      margin: 0;
      font-size: 18px;
    }
    
    .user-info {
      .el-dropdown-link {
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
