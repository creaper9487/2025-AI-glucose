<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import axiosInstance from '@/components/axiosInstance'
import { useWalletStore } from '@/stores/walletStore'
import { useMedescienetStore } from '@/stores/medescienetStore'
import {
  estimateUploadFee,
  extractErrorMessage,
  fetchCurrentWalrusEpoch,
  InsufficientVaultError,
  ServiceExpiredError,
  uploadGlucoseData,
  VaultNotCreatedError,
  WalletNotLinkedError,
} from '@/lib/medescienetUploader'

const router = useRouter()
const walletStore = useWalletStore()
const medescienetStore = useMedescienetStore()

const currentStep = ref(1)
const uploadStage = ref('idle')
const requiresVaultCreation = ref(false)
const startDate = ref('')
const endDate = ref('')
const epochs = ref(3)
const consentChecked = ref(false)

const preview = ref(null)
const packagePayload = ref(null)
const uploads = ref([])
const currentWalrusEpoch = ref(0)
const uploadDraft = ref(null)
const capResult = ref(null)
const uploadResult = ref(null)

const error = ref('')
const step3Loading = ref(false)
const previewLoading = ref(false)
const uploadLoading = ref(false)
const loadingUploads = ref(false)

const stageLabelMap = {
  idle: '尚未開始',
  vault_check: '檢查訂閱與 Vault',
  encrypting: '加密資料中',
  uploading: '寫入 Walrus 中',
  certifying: '證明與認證中',
  confirming: '鏈上確認中',
  done: '完成',
  error: '失敗',
}

const feeEstimateMist = computed(() => {
  if (!preview.value?.estimated_size_bytes || !epochs.value) return 0n
  return estimateUploadFee(preview.value.estimated_size_bytes, Number(epochs.value))
})

const feeEstimateSui = computed(() => (Number(feeEstimateMist.value) / 1_000_000_000).toFixed(6))
const walrusEndEpoch = computed(() => currentWalrusEpoch.value + Number(epochs.value || 0))
const serviceStatus = computed(() => medescienetStore.subscriptionStatus || 'expired')

function resetUploadState() {
  uploadDraft.value = null
  capResult.value = null
  uploadResult.value = null
  uploadStage.value = 'idle'
}

function formatStatus(status) {
  return {
    pending: '上傳中',
    confirmed: '有效',
    expired: '已過期',
  }[status] || status
}

function mapErrorToBanner(err) {
  if (err instanceof ServiceExpiredError) {
    return '服務已過期，請先續約再嘗試上傳。'
  }
  if (err instanceof InsufficientVaultError) {
    return `Vault 餘額不足：需要 ${err.needed} MIST，現有 ${err.have} MIST。`
  }
  if (err instanceof VaultNotCreatedError) {
    requiresVaultCreation.value = true
    return '尚未建立 Vault，請先建立 Vault。'
  }
  if (err instanceof WalletNotLinkedError) {
    return '請先完成錢包綁定。'
  }
  return extractErrorMessage(err)
}

async function loadUploads() {
  loadingUploads.value = true
  try {
    const response = await axiosInstance.get('/api/medescienet/uploads/')
    uploads.value = response.data
  } catch (uploadError) {
    error.value = extractErrorMessage(uploadError)
  } finally {
    loadingUploads.value = false
  }
}

async function loadPreview() {
  error.value = ''
  previewLoading.value = true
  resetUploadState()

  try {
    const [previewResponse, epochValue] = await Promise.all([
      axiosInstance.get('/api/medescienet/export/preview/', {
        params: {
          start_date: startDate.value,
          end_date: endDate.value,
        },
      }),
      fetchCurrentWalrusEpoch(),
    ])

    preview.value = previewResponse.data
    currentWalrusEpoch.value = epochValue
    currentStep.value = 2
  } catch (previewError) {
    error.value = extractErrorMessage(previewError)
  } finally {
    previewLoading.value = false
  }
}

async function requestUploadCap() {
  error.value = ''
  step3Loading.value = true
  resetUploadState()

  try {
    const packageResponse = await axiosInstance.post('/api/medescienet/export/package/', {
      start_date: startDate.value,
      end_date: endDate.value,
    })
    packagePayload.value = packageResponse.data

    const uploadResponse = await axiosInstance.post('/api/medescienet/uploads/', {
      date_range_start: startDate.value,
      date_range_end: endDate.value,
      record_count: packageResponse.data.record_count,
      file_size_bytes: preview.value?.estimated_size_bytes || 0,
      schema_version: packageResponse.data.schema_version,
      checksum_sha256: packageResponse.data.checksum_sha256,
      status: 'pending',
    })
    uploadDraft.value = uploadResponse.data

    const capResponse = await medescienetStore.requestUploadCap()
    capResult.value = capResponse
    requiresVaultCreation.value = Boolean(capResponse?.requires_vault_creation)

    if (requiresVaultCreation.value) {
      currentStep.value = 1
      return
    }

    currentStep.value = 4
  } catch (capError) {
    error.value = extractErrorMessage(capError)
  } finally {
    step3Loading.value = false
  }
}

async function runUpload() {
  error.value = ''
  uploadLoading.value = true

  try {
    uploadResult.value = await uploadGlucoseData({
      walletStore,
      medescienetStore,
      axiosInstance,
      rawDataBase64: packagePayload.value.data,
      capObjectId: capResult.value.cap_object_id,
      subStateId: capResult.value.sub_state_id,
      walrusEpochs: Number(epochs.value),
      uploadId: uploadDraft.value.id,
      checksumSha256: packagePayload.value.checksum_sha256,
      onProgress: (stage) => {
        if (stage === 'seal') uploadStage.value = 'encrypting'
        else if (stage === 'walrus-register' || stage === 'walrus-upload') uploadStage.value = 'uploading'
        else if (stage === 'walrus-certify') uploadStage.value = 'certifying'
        else if (stage === 'chain') uploadStage.value = 'confirming'
        else if (stage === 'done') uploadStage.value = 'done'
        else uploadStage.value = stage
      },
    })

    await loadUploads()
  } catch (uploadError) {
    uploadStage.value = 'error'
    error.value = mapErrorToBanner(uploadError)
  } finally {
    uploadLoading.value = false
  }
}

onMounted(async () => {
  walletStore.initialize()
  if (!walletStore.address) {
    router.push('/profile?needWalletLinked=true')
    return
  }

  try {
    const sub = await medescienetStore.fetchSubscriptionStatus()
    requiresVaultCreation.value = !sub?.vault_id
  } catch (subError) {
    if (subError?.response?.status === 404) {
      requiresVaultCreation.value = true
    }
  }

  await loadUploads()
})
</script>

<template>
  <div class="mx-auto max-w-5xl p-6">
    <div class="mb-8 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
      <div>
        <div class="flex items-center gap-3">
          <h1 class="text-3xl font-semibold text-slate-900">mediSciNet 資料分享</h1>
          <span
            class="rounded-full px-3 py-1 text-xs font-semibold"
            :class="{
              'bg-emerald-100 text-emerald-800': serviceStatus === 'active',
              'bg-amber-100 text-amber-800': serviceStatus === 'expiring_soon',
              'bg-red-100 text-red-800': serviceStatus === 'expired'
            }"
          >
            {{ serviceStatus.toUpperCase() }}
          </span>
        </div>
        <p class="mt-2 text-sm text-slate-600">選擇血糖資料範圍、簽發上傳權限，並用 Sui 錢包完成加密與鏈上登錄。</p>
      </div>
      <div class="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm shadow-sm">
        <p class="text-slate-500">已連接錢包</p>
        <p class="font-mono text-slate-900">{{ walletStore.address || '尚未連接' }}</p>
      </div>
    </div>

    <div v-if="error" class="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
      {{ error }}
    </div>

    <div v-if="requiresVaultCreation" class="mb-6 flex items-center justify-between rounded-2xl border border-amber-200 bg-amber-50 px-5 py-4">
      <div>
        <p class="font-medium text-amber-900">需要建立 Vault</p>
        <p class="text-sm text-amber-700">上傳前需要先建立 Vault。</p>
      </div>
      <router-link to="/profile?tab=medescienet" class="rounded-xl bg-amber-600 px-4 py-2 text-sm font-medium text-white hover:bg-amber-700">
        建立 Vault
      </router-link>
    </div>

    <div v-else-if="feeEstimateMist > 0 && BigInt(medescienetStore.vaultBalance || 0) < feeEstimateMist" class="mb-6 flex items-center justify-between rounded-2xl border border-amber-200 bg-amber-50 px-5 py-4">
      <div>
        <p class="font-medium text-amber-900">Vault 餘額不足</p>
        <p class="text-sm text-amber-700">預估上傳需 {{ feeEstimateSui }} SUI，請先儲值。</p>
      </div>
      <router-link to="/profile?tab=medescienet" class="rounded-xl bg-amber-600 px-4 py-2 text-sm font-medium text-white hover:bg-amber-700">
        Top Up Vault
      </router-link>
    </div>

    <div class="grid gap-6 lg:grid-cols-[1.3fr_0.9fr]">
      <section class="space-y-6">
        <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="text-xl font-semibold text-slate-900">Step 1：選擇資料範圍</h2>
          <div class="mt-5 grid gap-4 md:grid-cols-2">
            <div>
              <label class="text-sm font-medium text-slate-700">開始日期</label>
              <input v-model="startDate" type="date" class="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3" />
            </div>
            <div>
              <label class="text-sm font-medium text-slate-700">結束日期</label>
              <input v-model="endDate" type="date" class="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3" />
            </div>
          </div>

          <div class="mt-4 grid gap-4 md:grid-cols-2">
            <div>
              <label class="text-sm font-medium text-slate-700">保留 Epoch 數量</label>
              <input v-model="epochs" type="number" min="1" class="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3" />
            </div>
            <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
              <p class="text-sm text-slate-500">目前 Walrus Epoch</p>
              <p class="mt-1 font-mono text-lg text-slate-900">{{ currentWalrusEpoch || '-' }}</p>
            </div>
          </div>

          <button class="mt-5 rounded-2xl bg-slate-900 px-5 py-3 text-sm font-medium text-white" :disabled="previewLoading || !startDate || !endDate" @click="loadPreview">
            {{ previewLoading ? '讀取預覽中...' : '查詢預覽' }}
          </button>

          <div v-if="preview" class="mt-6 rounded-2xl border border-slate-200 bg-slate-50 p-5">
            <p class="text-sm text-slate-600">資料筆數：{{ preview.record_count }}</p>
            <p class="text-sm text-slate-600">預估大小：{{ preview.estimated_size_bytes }} bytes</p>
            <p class="text-sm text-slate-600">預估上傳費：{{ feeEstimateSui }} SUI</p>
          </div>
        </div>

        <div v-if="preview" class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="text-xl font-semibold text-slate-900">Step 2：狀態檢查與同意資料分享</h2>

          <div v-if="serviceStatus === 'expired'" class="mt-4 rounded-xl border border-red-200 bg-red-50 p-4">
            <p class="font-medium text-red-900">服務已過期，無法分享資料</p>
            <p class="mt-1 text-sm text-red-700">請先續約後再上傳。</p>
            <router-link to="/profile?tab=medescienet" class="mt-3 inline-block rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700">Renew Service</router-link>
          </div>

          <div v-else>
            <div v-if="serviceStatus === 'expiring_soon'" class="mt-4 mb-4 rounded-xl border border-amber-200 bg-amber-50 p-4">
              <p class="font-medium text-amber-900">服務即將到期</p>
              <p class="mt-1 text-sm text-amber-700">建議先續約，避免分享流程中斷。</p>
              <router-link to="/profile?tab=medescienet" class="mt-3 inline-block rounded-lg bg-amber-600 px-4 py-2 text-sm font-medium text-white hover:bg-amber-700">Renew Service</router-link>
            </div>

            <label class="mt-5 flex items-start gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4">
              <input v-model="consentChecked" type="checkbox" class="mt-1 h-4 w-4 rounded border-slate-300" />
              <span class="text-sm text-slate-700">我已閱讀並同意將所選血糖資料分享至 mediSciNet。</span>
            </label>

            <div class="mt-5 flex gap-3">
              <button class="rounded-2xl border border-slate-300 px-4 py-3 text-sm font-medium text-slate-700" @click="currentStep = 1">返回修改</button>
              <button class="rounded-2xl bg-emerald-600 px-5 py-3 text-sm font-medium text-white disabled:opacity-50" :disabled="!consentChecked || step3Loading" @click="requestUploadCap">
                {{ step3Loading ? '簽發中...' : '同意並請求 Upload Cap' }}
              </button>
            </div>
          </div>
        </div>

        <div v-if="capResult" class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="text-xl font-semibold text-slate-900">Step 3：Upload Cap 已就緒</h2>
          <div class="mt-4 grid gap-4 md:grid-cols-2">
            <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
              <p class="text-xs uppercase tracking-wide text-slate-500">Cap Object ID</p>
              <p class="mt-2 break-all font-mono text-sm text-slate-900">{{ capResult.cap_object_id }}</p>
            </div>
            <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
              <p class="text-xs uppercase tracking-wide text-slate-500">Sub State ID</p>
              <p class="mt-2 break-all font-mono text-sm text-slate-900">{{ capResult.sub_state_id || '-' }}</p>
            </div>
          </div>
          <button class="mt-5 rounded-2xl bg-slate-900 px-5 py-3 text-sm font-medium text-white" :disabled="uploadLoading" @click="runUpload">
            {{ uploadLoading ? '上傳中...' : '開始加密上傳' }}
          </button>
        </div>

        <div v-if="capResult" class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="text-xl font-semibold text-slate-900">Step 4：上傳進度</h2>
          <div class="mt-4 space-y-3">
            <div class="flex items-center justify-between rounded-2xl border border-slate-200 px-4 py-3 bg-slate-50">
              <span class="text-sm text-slate-700">目前階段</span>
              <span class="text-sm font-medium text-slate-900">{{ stageLabelMap[uploadStage] || uploadStage }}</span>
            </div>
            <div v-if="uploadStage !== 'idle' && uploadStage !== 'error' && uploadStage !== 'done'" class="flex justify-center p-4">
              <div class="h-6 w-6 animate-spin rounded-full border-2 border-slate-300 border-t-slate-800"></div>
            </div>
            <div v-if="uploadStage === 'error'" class="rounded-2xl border border-red-200 bg-red-50 p-4">
              <p class="text-sm font-medium text-red-700">發生錯誤，上傳中止。</p>
            </div>
          </div>

          <div v-if="uploadResult" class="mt-6 rounded-2xl border border-emerald-200 bg-emerald-50 p-5">
            <p class="text-lg font-semibold text-emerald-800">上傳成功</p>
            <p class="mt-3 text-sm text-emerald-700">Blob ID：{{ uploadResult.blobId }}</p>
            <p class="mt-1 text-sm text-emerald-700">Seal ID：{{ uploadResult.sealId }}</p>
            <p class="mt-1 text-sm text-emerald-700">Walrus End Epoch：{{ uploadResult.walrusEndEpoch }}</p>
            <p class="mt-1 text-sm text-emerald-700">On-chain Digest：{{ uploadResult.txDigest }}</p>
          </div>
        </div>
      </section>

      <aside class="space-y-6">
        <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold text-slate-900">我的共享資料</h2>
            <button class="text-sm text-blue-600" :disabled="loadingUploads" @click="loadUploads">重新整理</button>
          </div>

          <div v-if="loadingUploads" class="mt-4 text-sm text-slate-500">載入中...</div>
          <div v-else-if="!uploads.length" class="mt-4 rounded-2xl border border-dashed border-slate-300 px-4 py-5 text-sm text-slate-500">目前還沒有 mediSciNet 上傳紀錄。</div>
          <div v-else class="mt-4 space-y-3">
            <div v-for="upload in uploads" :key="upload.id" class="rounded-2xl border border-slate-200 p-4">
              <div class="flex items-start justify-between gap-3">
                <div>
                  <p class="font-medium text-slate-900">{{ upload.date_range_start }} ~ {{ upload.date_range_end }}</p>
                  <p class="mt-1 text-sm text-slate-500">{{ upload.record_count }} 筆 · {{ upload.file_size_bytes }} bytes</p>
                </div>
                <span class="rounded-full px-3 py-1 text-xs font-medium" :class="upload.status === 'confirmed' ? 'bg-emerald-100 text-emerald-700' : upload.status === 'expired' ? 'bg-amber-100 text-amber-700' : 'bg-blue-100 text-blue-700'">
                  {{ formatStatus(upload.status) }}
                </span>
              </div>

              <p v-if="upload.blob_id" class="mt-3 break-all font-mono text-xs text-slate-600">Blob: {{ upload.blob_id }}</p>
              <p v-if="upload.user_file_cap_id" class="mt-1 break-all font-mono text-xs text-slate-600">Cap: {{ upload.user_file_cap_id }}</p>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>
