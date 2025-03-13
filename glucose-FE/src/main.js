import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import i18nInstance from './components/i18nUtil.js'
import { useAuthStore } from './stores/authStore' // 引入認證存儲

const app = createApp(App)
app.use(createPinia())

// 添加全局導航守衛
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // 檢查路由是否需要認證以及用戶是否已登入
  if (to.meta.requiresAuth && (!authStore.token || authStore.token.length === 0)) {
    // 如果路由需要認證且用戶未登入，重定向到登入頁面並帶上提示參數
    next({ path: '/login', query: { redirect: to.fullPath, needAuth: 'true' } })
  } else {
    // 允許導航繼續
    next()
  }
})

app.use(router)
app.use(i18nInstance)
app.mount('#app')