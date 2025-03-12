<script setup>
import LoginForm from '@/components/Forms/loginForm.vue';
import NavBar from '@/components/NavBar.vue';
import aboutUsBtn from '@/components/aboutUsBtn.vue';
</script>

<template>
  <NavBar></NavBar>
  <div class="h-screen flex items-center justify-center bg-gray-500">
    <div class="flex flex-col items-center w-1/3">
      <LoginForm />
      <div class="mt-6 text-center">
        <p class="text-sm text-white">
          還沒有帳戶？
          <RouterLink to="/register" class="font-medium text-blue-100 hover:text-blue-200 transition duration-200">
            立即註冊
          </RouterLink>
        </p>
      </div>
    </div>
  </div>
  <div class="fixed bottom-0 right-0 p-4 mr-8 mb-4"><aboutUsBtn /></div>
</template>