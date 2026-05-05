<script setup>
import NavBar from '@/components/NavBar.vue';
</script>

<template>
  <NavBar class="fixed w-full top-0 z-50 bg-white/80 backdrop-blur-md border-b border-gray-100" />
  
  <div class="relative w-full min-h-screen bg-slate-50 flex items-center pt-16">
    <!-- Background Decoration -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-1/4 -right-1/4 w-[800px] h-[800px] rounded-full bg-blue-50 opacity-50 blur-3xl"></div>
      <div class="absolute -bottom-1/4 -left-1/4 w-[600px] h-[600px] rounded-full bg-teal-50 opacity-50 blur-3xl"></div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full z-10 flex flex-col md:flex-row items-center gap-12">
      
      <!-- Text content -->
      <div class="flex-1 flex flex-col items-start text-left">
        <div class="inline-flex items-center px-3 py-1 mb-6 text-sm font-semibold text-blue-800 bg-blue-100 rounded-full">
          ✨ Next-Gen AI Health
        </div>
        
        <h1 class="text-6xl md:text-7xl font-extrabold tracking-tight text-slate-900 mb-4">
          DIA<span class="text-blue-600">BEAT</span>
        </h1>
        
        <h2 class="text-2xl md:text-3xl font-semibold text-slate-700 mb-6">
          打敗糖：人工智慧血糖管理工具
        </h2>
        
        <p class="text-lg text-slate-600 mb-10 leading-relaxed max-w-xl">
          運用先進的人工智慧與數據分析技術，精準預測並管理您的血糖波動。結合去中心化技術，確保個人醫療數據絕對隱私與安全。
        </p>
        
        <div class="flex flex-col sm:flex-row gap-4 w-full sm:w-auto">
          <router-link to="/register" class="flex items-center justify-center px-8 py-3.5 text-base font-medium text-white bg-blue-600 rounded-xl hover:bg-blue-700 shadow-lg shadow-blue-500/30 transition-all duration-300 hover:-translate-y-0.5">
            立即開始
            <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 ml-2" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </router-link>
          <router-link to="/login" class="flex items-center justify-center px-8 py-3.5 text-base font-medium text-slate-700 bg-white border border-slate-200 rounded-xl hover:bg-slate-50 hover:text-blue-600 transition-all duration-300">
            登入帳號
          </router-link>
        </div>

        <!-- Trust Indicators -->
        <div class="mt-12 pt-8 border-t border-slate-200">
          <p class="text-sm font-medium text-slate-500 mb-4">結合最先進的安全區塊鏈技術</p>
          <div class="flex items-center gap-6 grayscale opacity-60">
             <!-- Placeholder tech logos -->
             <div class="text-xl font-bold font-mono text-slate-700 flex items-center"><span class="text-blue-600 mr-1">◎</span> Sui Blockchain</div>
             <div class="text-xl font-bold font-mono text-slate-700 flex items-center"><span class="text-teal-500 mr-1">▲</span> AI Model</div>
          </div>
        </div>
      </div>
      
      <!-- Hero Image -->
      <div class="flex-1 relative w-full flex justify-center mt-12 md:mt-0">
        <div class="relative w-full max-w-lg">
          <div class="absolute inset-0 bg-gradient-to-tr from-blue-500/20 to-teal-400/20 rounded-3xl transform translate-x-4 translate-y-4"></div>
          <div class="relative rounded-3xl overflow-hidden shadow-2xl border border-white/50 bg-white">
            <img src="../assets/ggimg.png" class="w-full h-auto object-cover transform hover:scale-105 transition-transform duration-[2000ms]" alt="DIABEAT Dashboard Preview" />
            <div class="absolute inset-0 ring-1 ring-inset ring-black/10 rounded-3xl"></div>
          </div>
          
          <!-- Floating UI Element -->
          <div class="absolute -left-8 top-1/4 bg-white/90 backdrop-blur pb-3 pt-4 px-4 rounded-xl shadow-xl border border-white flex flex-col gap-2 animate-bounce flex items-center justify-center" style="animation-duration: 4s;">
            <div class="text-xs font-semibold text-slate-500 uppercase tracking-wider">血糖預測</div>
            <div class="text-2xl font-bold text-blue-600 font-mono">102 <span class="text-sm font-medium text-slate-400">mg/dL</span></div>
            <div class="text-xs font-bold text-emerald-500 px-2 py-1 bg-emerald-50 rounded-md">穩定</div>
          </div>
        </div>
      </div>
      
    </div>
  </div>
</template>