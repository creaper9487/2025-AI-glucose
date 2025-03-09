import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import FigureView from '@/views/FigureView.vue'
import LoginView from '@/views/LoginView.vue'
import GoogleOauth from '@/views/googleOauth.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/figure',
      name: 'figure',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: FigureView,
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path:'/oauth-callback',
      name: 'oauth-callback',
      component: GoogleOauth,
    }
  ],
})

export default router
