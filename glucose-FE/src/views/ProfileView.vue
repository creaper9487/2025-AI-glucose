<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import ModelTrainingProgress from '@/components/ModelTrainingProgress.vue'
import axiosInstance from '@/components/axiosInstance'
import { useWalletStore } from '@/stores/walletStore'
import { useDataStore } from '@/stores/dataStore'
import { useMedescienetStore } from '@/stores/medescienetStore'

const route = useRoute()
const dataStore = useDataStore()
const walletStore = useWalletStore()
const medescienetStore = useMedescienetStore()

const userData = ref({
  weight: dataStore.profile.weight || '',
  height: dataStore.profile.height || '',
  age: dataStore.profile.age || '',
  gender: '',
  diabetesType: '',
  activityLevel: '1',
})

const saveMessage = ref('')
const showMessage = ref(false)
const walletStatus = ref({ linked: false, wallet_address: null, linked_at: null })
const walletError = ref('')
const walletLoading = ref(false)

const showWalletHint = computed(() => route.query.tab === 'medescienet' || route.query.needWalletLinked === 'true')

const showManageModal = ref(false)

function formatSui(mist) {
  if (mist == null) return '0'
  return (Number(mist) / 1_000_000_000).toFixed(4)
}

function toBase64(bytes) {
  if (!bytes) return null
  if (typeof bytes === 'string') return bytes
  const arr = bytes instanceof ArrayBuffer ? new Uint8Array(bytes) : bytes
  let binary = ''
  for (let index = 0; index < arr.length; index += 1) {
    binary += String.fromCharCode(arr[index])
  }
  return btoa(binary)
}

async function fetchWalletStatus() {
  walletLoading.value = true
  try {
    const response = await axiosInstance.get('/api/medescienet/wallet/status/')
    walletStatus.value = response.data
  } catch (error) {
    walletError.value = error?.response?.data?.error || error?.message || '無法讀取錢包綁定狀態'
  } finally {
    walletLoading.value = false
  }
}

async function linkWallet() {
  walletError.value = ''

  if (!walletStore.address) {
    await walletStore.connect()
  }
  if (!walletStore.address) {
    walletError.value = walletStore.error || '請先連接錢包'
    return
  }

  try {
    const nonceResponse = await axiosInstance.post('/api/medescienet/wallet/nonce/', {
      wallet_address: walletStore.address,
    })
    const nonce = nonceResponse.data.nonce

    const signResult = await walletStore.signMessage(nonce)
    await axiosInstance.post('/api/medescienet/wallet/link/', {
      wallet_address: walletStore.address,
      signature: toBase64(signResult.signature),
      public_key: toBase64(signResult.publicKey),
    })

    await fetchWalletStatus()
  } catch (error) {
    walletError.value = error?.response?.data?.error || error?.message || '錢包綁定失敗'
  }
}

async function unlinkWallet() {
  walletError.value = ''
  try {
    await axiosInstance.delete('/api/medescienet/wallet/unlink/')
    await fetchWalletStatus()
  } catch (error) {
    walletError.value = error?.response?.data?.error || error?.message || '解除綁定失敗'
  }
}

function saveProfile() {
  dataStore.updateProfile(userData.value)
  saveMessage.value = '資料已成功保存！'
  showMessage.value = true
  setTimeout(() => {
    showMessage.value = false
  }, 3000)
}

onMounted(async () => {
  walletStore.initialize()
  await fetchWalletStatus()
  try {
    await medescienetStore.fetchSubscriptionStatus()
  } catch (err) {
    console.error(err)
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-800 text-white">
    <NavBar />

    <div class="mx-auto max-w-7xl px-4 pb-12 pt-20 sm:px-6 lg:px-8">
      <h1 class="mb-8 text-3xl font-bold text-blue-400">個人資料儀表板</h1>

      <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div class="col-span-2 space-y-6">
          <section class="rounded-xl border border-gray-700 bg-gray-800 bg-opacity-70 p-6 shadow-lg backdrop-blur-sm">
            <h2 class="mb-6 flex items-center text-xl font-semibold">
              <svg xmlns="http://www.w3.org/2000/svg" class="mr-2 h-6 w-6 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              個人基本資料
            </h2>

            <div
              v-if="showMessage"
              class="mb-4 flex items-center rounded-lg border border-green-500 bg-green-500 bg-opacity-20 p-3 text-green-400"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="mr-2 h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              {{ saveMessage }}
            </div>

            <div class="grid gap-6 md:grid-cols-2">
              <div>
                <label class="mb-2 block text-sm text-gray-300">體重 (公斤)</label>
                <input v-model="userData.weight" type="number" class="w-full rounded-lg border border-gray-600 bg-gray-700 px-4 py-2 text-white transition-colors focus:border-blue-500 focus:outline-none" placeholder="請輸入體重" />
              </div>
              <div>
                <label class="mb-2 block text-sm text-gray-300">身高 (公分)</label>
                <input v-model="userData.height" type="number" class="w-full rounded-lg border border-gray-600 bg-gray-700 px-4 py-2 text-white transition-colors focus:border-blue-500 focus:outline-none" placeholder="請輸入身高" />
              </div>
              <div>
                <label class="mb-2 block text-sm text-gray-300">年齡</label>
                <input v-model="userData.age" type="number" class="w-full rounded-lg border border-gray-600 bg-gray-700 px-4 py-2 text-white transition-colors focus:border-blue-500 focus:outline-none" placeholder="請輸入年齡" />
              </div>
            </div>

            <button @click="saveProfile" class="mt-8 rounded-lg bg-blue-600 px-6 py-3 font-medium shadow-lg transition-all duration-300 hover:-translate-y-1 hover:bg-blue-700 hover:shadow-xl">
              保存資料
            </button>
          </section>

          <section
            class="rounded-xl border p-6 shadow-lg backdrop-blur-sm"
            :class="showWalletHint ? 'border-emerald-500 bg-emerald-500/10' : 'border-gray-700 bg-gray-800 bg-opacity-70'"
          >
            <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
              <div>
                <h2 class="text-xl font-semibold text-white">mediSciNet 帳號</h2>
                <p class="mt-2 text-sm text-slate-300">綁定 Sui 錢包後，才能將血糖資料加密上傳到 mediSciNet。</p>
                <p v-if="showWalletHint" class="mt-2 text-sm text-emerald-300">此路徑需要先完成錢包綁定。</p>
              </div>

              <div class="rounded-2xl border border-slate-600 bg-slate-900/60 px-4 py-3 text-sm">
                <p class="text-slate-400">錢包連接</p>
                <p class="mt-1 font-mono text-white">{{ walletStore.address || '尚未連接' }}</p>
              </div>
            </div>

            <div class="mt-5 rounded-2xl border border-slate-700 bg-slate-900/50 p-4">
              <p class="text-sm">
                綁定狀態：
                <strong v-if="walletStatus.linked" class="text-emerald-300">已綁定</strong>
                <strong v-else class="text-amber-300">未綁定</strong>
              </p>
              <p v-if="walletStatus.wallet_address" class="mt-2 break-all font-mono text-xs text-slate-300">
                地址：{{ walletStatus.wallet_address }}
              </p>
              <p v-if="walletStatus.linked_at" class="mt-1 text-xs text-slate-400">綁定時間：{{ walletStatus.linked_at }}</p>
            </div>

            <div class="mt-5 flex flex-wrap gap-3">
              <button class="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white" :disabled="walletLoading" @click="linkWallet">
                {{ walletLoading ? '處理中...' : '連接並綁定錢包' }}
              </button>
              <button class="rounded-lg border border-slate-500 px-4 py-2 text-sm text-slate-200" :disabled="walletLoading || !walletStatus.linked" @click="unlinkWallet">
                解除綁定
              </button>
              <router-link to="/share-data" class="rounded-lg border border-blue-400 px-4 py-2 text-sm text-blue-300">
                前往分享資料頁面
              </router-link>
            </div>

            <div v-if="walletError" class="mt-4 rounded-lg bg-red-500/20 p-3 text-sm text-red-300">
              {{ walletError }}
            </div>
          </section>

          <section class="rounded-xl border border-gray-700 bg-gray-800 bg-opacity-70 p-6 shadow-lg backdrop-blur-sm">
            <div class="flex items-center justify-between">
              <h2 class="text-xl font-semibold text-white">Service Status</h2>
            </div>
            <div class="mt-4 space-y-3 text-sm text-slate-300">
              <div class="flex items-center justify-between">
                <span>Status:</span>
                <span class="font-medium" :class="{
                  'text-emerald-400': medescienetStore.subscriptionStatus === 'active',
                  'text-amber-400': medescienetStore.subscriptionStatus === 'expiring_soon',
                  'text-slate-400': medescienetStore.subscriptionStatus === 'expired'
                }">{{ medescienetStore.subscriptionStatus.toUpperCase() }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span>Projected service end:</span>
                <span class="font-mono">epoch {{ medescienetStore.projectedServiceEndEpoch || '-' }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span>Vault balance:</span>
                <span><span class="font-mono text-emerald-300">{{ formatSui(medescienetStore.vaultBalance) }}</span> SUI (withdrawable)</span>
              </div>
              <div class="flex items-center justify-between">
                <span>Service credit:</span>
                <span><span class="font-mono text-blue-300">{{ formatSui(medescienetStore.serviceCreditBalance) }}</span> SUI (non-withdrawable)</span>
              </div>
              <div class="mt-5 border-t border-slate-700 pt-4">
                <button
                  class="rounded-lg bg-blue-600 px-5 py-2 font-medium text-white transition-colors hover:bg-blue-700"
                  @click="showManageModal = true"
                >
                  Manage
                </button>
              </div>
            </div>
          </section>

          <!-- Manage Modal -->
          <div v-if="showManageModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm px-4">
            <div class="w-full max-w-md rounded-2xl border border-slate-700 bg-slate-800 p-6 shadow-2xl">
              <div class="flex items-center justify-between">
                <h3 class="text-xl font-bold text-white">Manage Service</h3>
                <button @click="showManageModal = false" class="text-slate-400 hover:text-white">✕</button>
              </div>
              <div class="mt-4 flex flex-col gap-3">
                <button class="rounded-xl border border-slate-600 bg-slate-700 px-4 py-3 text-left font-medium text-white hover:bg-slate-600">
                  Withdraw from Vault
                </button>
                <button class="rounded-xl border border-slate-600 bg-slate-700 px-4 py-3 text-left font-medium text-white hover:bg-slate-600">
                  Approve Settlement
                </button>
                <button class="rounded-xl border border-slate-600 bg-slate-700 px-4 py-3 text-left font-medium text-white hover:bg-slate-600">
                  View History
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="col-span-1 space-y-6">
          <div class="rounded-xl border border-gray-700 bg-gray-800 bg-opacity-70 p-6 shadow-lg backdrop-blur-sm">
            <h2 class="mb-4 flex items-center text-xl font-semibold">
              <svg xmlns="http://www.w3.org/2000/svg" class="mr-2 h-6 w-6 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
              </svg>
              AI模型訓練進度
            </h2>

            <ModelTrainingProgress />

            <p class="mt-4 text-sm text-gray-400">
              系統正在根據你的數據訓練個人化血糖預測模型。
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
input:focus,
select:focus {
  border-color: #03e9f4;
  box-shadow: 0 0 5px rgba(3, 233, 244, 0.5);
}

input[type='number']::-webkit-inner-spin-button,
input[type='number']::-webkit-outer-spin-button {
  opacity: 1;
  background: #555;
  border-radius: 2px;
  height: 14px;
}
</style>
