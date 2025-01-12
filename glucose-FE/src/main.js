import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import i18nInstance from './components/i18nUtil.js'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(i18nInstance)
app.mount('#app')
