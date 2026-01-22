import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'

import App from './App.vue'
import './styles/index.scss'

// Vant UI
import {
  Button,
  NavBar,
  Cell,
  CellGroup,
  List,
  Image,
  Card,
  Row,
  Col,
  NoticeBar,
  Dialog,
  Form,
  Field,
  RadioGroup,
  Radio,
  Toast,
  Loading,
  PullRefresh,
  Tabbar,
  TabbarItem,
  Tag,
  Progress,
  Search,
  Icon,
  Popup,
  Empty,
  Skeleton,
  ActionSheet
} from 'vant'

import 'vant/lib/index.css'

const app = createApp(App)

// 注册Vant组件
app.use(Button)
   .use(NavBar)
   .use(Cell)
   .use(CellGroup)
   .use(List)
   .use(Image)
   .use(Card)
   .use(Row)
   .use(Col)
   .use(NoticeBar)
   .use(Dialog)
   .use(Form)
   .use(Field)
   .use(RadioGroup)
   .use(Radio)
   .use(Toast)
   .use(Loading)
   .use(PullRefresh)
   .use(Tabbar)
   .use(TabbarItem)
   .use(Tag)
   .use(Progress)
   .use(Search)
   .use(Icon)
   .use(Popup)
   .use(Empty)
   .use(Skeleton)
   .use(ActionSheet)

app.use(createPinia())

// 路由
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('./views/Home.vue'),
    meta: { title: '活动列表' }
  },
  {
    path: '/vote/:serviceId',
    name: 'Vote',
    component: () => import('./views/Vote.vue'),
    meta: { title: '投票' }
  },
  {
    path: '/candidate/:serviceId/:candidateId',
    name: 'Candidate',
    component: () => import('./views/Candidate.vue'),
    meta: { title: '候选人详情' }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('./views/Search.vue'),
    meta: { title: '搜索' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('./views/Profile.vue'),
    meta: { title: '个人中心' }
  },
  {
    path: '/ranking/:serviceId',
    name: 'Ranking',
    component: () => import('./views/Ranking.vue'),
    meta: { title: '排行榜' }
  },
  {
    path: '/statistics/:serviceId',
    name: 'Statistics',
    component: () => import('./views/Statistics.vue'),
    meta: { title: '数据统计' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

app.use(router)

app.mount('#app')