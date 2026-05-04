<script setup>
import { computed, onMounted, ref } from 'vue'
import { useWalletStore } from '@/stores/walletStore'

const walletStore = useWalletStore()
const menuOpen = ref(false)

const displayAddress = computed(() => {
  if (!walletStore.address) {
    return ''
  }

  return `${walletStore.address.slice(0, 6)}...${walletStore.address.slice(-4)}`
})

const openMenu = async () => {
  menuOpen.value = !menuOpen.value
  if (menuOpen.value) {
    walletStore.refreshWallets()
  }
}

const connectWallet = async wallet => {
  await walletStore.connect(wallet)
  if (!walletStore.error) {
    menuOpen.value = false
  }
}

const disconnectWallet = async () => {
  await walletStore.disconnect()
  menuOpen.value = false
}

onMounted(() => {
  walletStore.initialize()
})
</script>

<template>
  <div class="relative">
    <button
      v-if="walletStore.address"
      class="inline-flex items-center rounded-full border border-white/30 bg-white/10 px-3 py-2 text-sm font-medium text-white transition hover:bg-white/20"
      @click="openMenu"
    >
      <span class="mr-2 h-2 w-2 rounded-full bg-emerald-300"></span>
      {{ displayAddress }}
    </button>

    <button
      v-else
      class="inline-flex items-center rounded-full border border-white/30 bg-white px-3 py-2 text-sm font-semibold text-teal-700 transition hover:bg-teal-50 disabled:cursor-not-allowed disabled:opacity-70"
      :disabled="walletStore.isConnecting"
      @click="openMenu"
    >
      <span v-if="walletStore.isConnecting">連接中...</span>
      <span v-else>連接 Sui 錢包</span>
    </button>

    <div
      v-if="menuOpen"
      class="absolute right-0 z-50 mt-2 w-72 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-2xl"
    >
      <div class="border-b border-slate-200 px-4 py-3">
        <p class="text-sm font-semibold text-slate-900">Sui 錢包</p>
        <p class="text-xs text-slate-500">選擇一個可用錢包進行連接</p>
      </div>

      <div v-if="walletStore.error" class="border-b border-red-100 bg-red-50 px-4 py-3 text-sm text-red-700">
        {{ walletStore.error }}
      </div>

      <div v-if="walletStore.wallets.length" class="max-h-72 overflow-auto py-2">
        <button
          v-for="wallet in walletStore.wallets"
          :key="wallet.id || wallet.name"
          class="flex w-full items-center gap-3 px-4 py-3 text-left transition hover:bg-slate-50"
          @click="connectWallet(wallet)"
        >
          <img
            v-if="wallet.icon"
            :src="wallet.icon"
            :alt="wallet.name"
            class="h-9 w-9 rounded-full border border-slate-200 bg-white object-cover"
          />
          <div v-else class="flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-sm font-bold text-slate-500">
            {{ wallet.name?.charAt(0) || 'W' }}
          </div>
          <div class="min-w-0 flex-1">
            <p class="truncate text-sm font-medium text-slate-900">{{ wallet.name }}</p>
            <p class="truncate text-xs text-slate-500">{{ wallet.id || 'wallet-standard' }}</p>
          </div>
        </button>
      </div>

      <div v-else class="px-4 py-5 text-sm text-slate-500">
        尚未偵測到可用的 Sui 錢包
      </div>

      <div v-if="walletStore.address" class="border-t border-slate-200 px-4 py-3">
        <button
          class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50"
          @click="disconnectWallet"
        >
          斷開連接
        </button>
      </div>
    </div>
  </div>
</template>