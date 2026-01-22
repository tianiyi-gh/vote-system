<template>
  <div id="app">
    <router-view v-slot="{ Component, route }">
      <transition :name="getTransitionName(route)">
        <component :is="Component" :key="route.fullPath" />
      </transition>
    </router-view>
    <van-tabbar v-model="active" fixed>
      <van-tabbar-item icon="home-o" to="/">活动列表</van-tabbar-item>
      <van-tabbar-item icon="search" to="/search">搜索</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { RouteLocationNormalized } from 'vue-router'

const active = ref(0)

const getTransitionName = (route: RouteLocationNormalized) => {
  // 主页到详情页：slide-right
  // 详情页返回主页：slide-left
  // 其他情况：fade
  const path = route.path
  if (path.startsWith('/vote/') || path.startsWith('/candidate/') || path.startsWith('/ranking/')) {
    return 'slide-right'
  }
  return 'fade'
}
</script>

<style>
@import './styles/transitions.scss';

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica, Segoe UI, Arial, Roboto, 'PingFang SC', 'miui', 'Hiragino Sans GB', 'Microsoft Yahei', sans-serif;
  background-color: #f7f8fa;
}

#app {
  min-height: 100vh;
  padding-bottom: 50px;
}
</style>