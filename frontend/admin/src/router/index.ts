import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      }
    ]
  },
  {
    path: '/activity',
    component: Layout,
    redirect: '/activity/list',
    meta: { title: '活动管理', icon: 'Operation' },
    children: [
      {
        path: 'list',
        name: 'ActivityList',
        component: () => import('@/views/activity/list.vue'),
        meta: { title: '活动列表', icon: 'List' }
      },
      {
        path: 'create',
        name: 'ActivityCreate',
        component: () => import('@/views/activity/create.vue'),
        meta: { title: '创建活动', icon: 'Plus' }
      },
      {
        path: 'edit/:id',
        name: 'ActivityEdit',
        component: () => import('@/views/activity/edit.vue'),
        meta: { title: '编辑活动', hidden: true }
      }
    ]
  },
  {
    path: '/candidate',
    component: Layout,
    redirect: '/candidate/list',
    meta: { title: '候选人管理', icon: 'User' },
    children: [
      {
        path: 'list',
        name: 'CandidateList',
        component: () => import('@/views/candidate/list.vue'),
        meta: { title: '候选人列表', icon: 'List' }
      }
    ]
  },
  {
    path: '/statistics',
    component: Layout,
    redirect: '/statistics/index',
    meta: { title: '数据统计', icon: 'DataAnalysis' },
    children: [
      {
        path: 'index',
        name: 'StatisticsIndex',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '统计分析', icon: 'TrendCharts' }
      }
    ]
  },
  {
    path: '/vote',
    component: Layout,
    redirect: '/vote/records',
    meta: { title: '投票管理', icon: 'Tickets' },
    children: [
      {
        path: 'records',
        name: 'VoteRecords',
        component: () => import('@/views/vote/records.vue'),
        meta: { title: '投票记录', icon: 'List' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
