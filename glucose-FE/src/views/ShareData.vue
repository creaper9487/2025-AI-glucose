<script setup>
import { computed, onMounted, ref } from 'vue'
import axiosInstance from '@/components/axiosInstance'
import { useWalletStore } from '@/stores/walletStore'
import { estimateUploadFee, extractErrorMessage, fetchCurrentWalrusEpoch, uploadGlucoseData } from '@/lib/medescienetUploader'

const walletStore = useWalletStore()

const currentStep = ref(1)
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

const step3Loading = ref(false)
const previewLoading = ref(false)
const uploadLoading = ref(false)
const loadingUploads = ref(false)
const error = ref('')

const progressState = ref({
  vault: 'idle',
  seal: 'idle',
  'walrus-register': 'idle',
  'walrus-upload': 'idle',
  'walrus-certify': 'idle',
  chain: 'idle',
  done: 'idle',
})

const feeEstimateMist = computed(() => {
  if (!preview.value?.estimated_size_bytes || !epochs.value) return 0n
  return estimateUploadFee(preview.value.estimated_size_bytes, epochs.value)
})

const feeEstimateSui = computed(() => (Number(feeEstimateMist.value) / 1_000_000_000).toFixed(6))
const walrusEndEpoch = computed(() => currentWalrusEpoch.value + Number(epochs.value || 0))

function resetUploadState() {
  uploadDraft.value = null
  capResult.value = null
  uploadResult.value = null
  progressState.value = {
    vault: 'idle',
    seal: 'idle',
    'walrus-register': 'idle',
    'walrus-upload': 'idle',
    'walrus-certify': 'idle',
    chain: 'idle',
    done: 'idle',
  }
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

    const capResponse = await axiosInstance.post('/api/medescienet/request-upload-cap/', {
      epochs: Number(epochs.value),
      walrus_end_epoch: walrusEndEpoch.value,
      upload_id: uploadResponse.data.id,
    })

    capResult.value = capResponse.data
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
      axiosInstance,
      rawDataBase64: packagePayload.value.data,
      capObjectId: capResult.value.cap_object_id,
      walrusEpochs: Number(epochs.value),
      uploadId: uploadDraft.value.id,
      checksumSha256: packagePayload.value.checksum_sha256,
      onProgress: (stage, value) => {
        progressState.value = {
          ...progressState.value,
          [stage]: value,
        }
      },
    })

    await loadUploads()
  } catch (uploadError) {
    error.value = extractErrorMessage(uploadError)
  } finally {
    uploadLoading.value = false
  }
}

function formatStatus(status) {
  return {
    pending: '上傳中',
    confirmed: '有效',
    expired: '已過期',
  }[status] || status
}

onMounted(async () => {
  walletStore.initialize()
  await loadUploads()
})
</script>

<template>
  <div class="mx-auto max-w-5xl p-6">
    <div class="mb-8 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
      <div>
        <h1 class="text-3xl font-semibold text-slate-900">mediSciNet 資料分享</h1>
        <p class="mt-2 text-sm text-slate-600">
          選擇血糖資料範圍、簽發上傳權限，並用你的 Sui 錢包完成加密與鏈上登錄。
        </p>
      </div>
      <div class="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm shadow-sm">
        <p class="text-slate-500">已連接錢包</p>
        <p class="font-mono text-slate-900">{{ walletStore.address || '尚未連接' }}</p>
      </div>
    </div>

    <div class="mb-6 grid gap-3 md:grid-cols-4">
      <div class="rounded-2xl border px-4 py-3" :class="currentStep >= 1 ? 'border-emerald-200 bg-emerald-50' : 'border-slate-200 bg-white'">
        <p class="text-xs uppercase tracking-wide text-slate-500">Step 1</p>
        <p class="mt-1 font-medium text-slate-900">選擇資料</p>
      </div>
      <div class="rounded-2xl border px-4 py-3" :class="currentStep >= 2 ? 'border-emerald-200 bg-emerald-50' : 'border-slate-200 bg-white'">
        <p class="text-xs uppercase tracking-wide text-slate-500">Step 2</p>
        <p class="mt-1 font-medium text-slate-900">同意條款</p>
      </div>
      <div class="rounded-2xl border px-4 py-3" :class="currentStep >= 3 ? 'border-emerald-200 bg-emerald-50' : 'border-slate-200 bg-white'">
        <p class="text-xs uppercase tracking-wide text-slate-500">Step 3</p>
        <p class="mt-1 font-medium text-slate-900">簽發 Upload Cap</p>
      </div>
      <div class="rounded-2xl border px-4 py-3" :class="currentStep >= 4 ? 'border-emerald-200 bg-emerald-50' : 'border-slate-200 bg-white'">
        <p class="text-xs uppercase tracking-wide text-slate-500">Step 4</p>
        <p class="mt-1 font-medium text-slate-900">加密並上傳</p>
      </div>
    </div>

    <div v-if="error" class="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
      {{ error }}
    </div>

    <div class="grid gap-6 lg:grid-cols-[1.3fr_0.9fr]">
      <section class="space-y-6">
        <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="text-xl font-semibold text-slate-900">Step 1/4：選擇資料範圍</h2>
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

          <button
            class="mt-5 rounded-2xl bg-slate-900 px-5 py-3 text-sm font-medium text-white"
            :disabled="previewLoading || !startDate || !endDate"
            @click="loadPreview"
          >
            {{ previewLoading ? '讀取預覽中...' : '查詢預覽' }}
          </button>

          <div v-if="preview" class="mt-6 rounded-2xl border border-slate-200 bg-slate-50 p-5">
            <div class="grid gap-4 md:grid-cols-3">
              <div>
                <p class="text-xs uppercase tracking-wide text-slate-500">紀錄數</p>
                <p class="mt-1 text-xl font-semibold text-slate-900">{{ preview.record_count }}</p>
              </div>
              <div>
                <p class="text-xs uppercase tracking-wide text-slate-500">預估大小</p>
                <p class="mt-1 text-xl font-semibold text-slate-900">{{ preview.estimated_size_bytes }} bytes</p>
              </div>
              <div>
                <p class="text-xs uppercase tracking-wide text-slate-500">預估鏈上費用</p>
                <p class="mt-1 text-xl font-semibold text-slate-900">{{ feeEstimateSui }} SUI</p>
              </div>
            </div>
            <p class="mt-4 text-sm text-slate-600">
              上傳有效至 Walrus epoch {{ walrusEndEpoch }}，約為目前 epoch 往後 {{ epochs }} 個 epoch。
            </p>
            <pre class="mt-4 max-h-64 overflow-auto rounded-2xl bg-slate-900 p-4 text-xs text-slate-100">{{ JSON.stringify(preview.sample, null, 2) }}</pre>
          </div>
        </div>

        <div v-if="preview" class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="text-xl font-semibold text-slate-900">Step 2/4：同意資料分享條款</h2>
          <div class="mt-4 space-y-3 text-sm leading-6 text-slate-600">
            <p>資料會先在瀏覽器端使用 Seal 加密，再寫入 Walrus 分散式儲存。</p>
            <p>研究者需取得時間限制的 `DataAccessCap` 才能解密，且無法從資料中直接反查你的 glucose 帳號。</p>
            <p>你的帳號識別會以 `user_hash` 匿名化後寫入資料集，用於長期研究追蹤。</p>
            <p>若資料或權限到期，可稍後重新上傳新的資料區段。</p>
          </div>

          <label class="mt-5 flex items-start gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4">
            <input v-model="consentChecked" type="checkbox" class="mt-1 h-4 w-4 rounded border-slate-300" />
            <span class="text-sm text-slate-700">
              我已閱讀並同意以上條款，同意將所選血糖資料分享至 mediSciNet 去中心化資料市場。
            </span>
          </label>

          <div class="mt-5 flex gap-3">
            <button class="rounded-2xl border border-slate-300 px-4 py-3 text-sm font-medium text-slate-700" @click="currentStep = 1">
              返回修改
            </button>
            <button
              class="rounded-2xl bg-emerald-600 px-5 py-3 text-sm font-medium text-white"
              :disabled="!consentChecked || step3Loading"
              @click="requestUploadCap"
            >
              {{ step3Loading ? '簽發中...' : '同意並請求 Upload Cap' }}
            </button>
          </div>
        </div>

        <div v-if="capResult" class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="text-xl font-semibold text-slate-900">Step 3/4：Upload Cap 已就緒</h2>
          <div class="mt-4 grid gap-4 md:grid-cols-2">
            <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
              <p class="text-xs uppercase tracking-wide text-slate-500">Cap Object ID</p>
              <p class="mt-2 break-all font-mono text-sm text-slate-900">{{ capResult.cap_object_id }}</p>
            </div>
            <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
              <p class="text-xs uppercase tracking-wide text-slate-500">Tx Digest</p>
              <p class="mt-2 break-all font-mono text-sm text-slate-900">{{ capResult.tx_digest }}</p>
            </div>
          </div>
          <button
            class="mt-5 rounded-2xl bg-slate-900 px-5 py-3 text-sm font-medium text-white"
            :disabled="uploadLoading"
            @click="runUpload"
          >
            {{ uploadLoading ? '上傳中...' : '開始加密上傳' }}
          </button>
        </div>

        <div v-if="capResult" class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="text-xl font-semibold text-slate-900">Step 4/4：上傳進度</h2>
          <div class="mt-4 space-y-3">
            <div class="flex items-center justify-between rounded-2xl border border-slate-200 px-4 py-3">
              <span class="text-sm text-slate-700">建立 / 檢查 Vault</span>
              <span class="text-sm font-medium text-slate-900">{{ progressState.vault }}</span>
            </div>
            <div class="flex items-center justify-between rounded-2xl border border-slate-200 px-4 py-3">
              <span class="text-sm text-slate-700">Seal 加密</span>
              <span class="text-sm font-medium text-slate-900">{{ progressState.seal }}</span>
            </div>
            <div class="flex items-center justify-between rounded-2xl border border-slate-200 px-4 py-3">
              <span class="text-sm text-slate-700">Walrus 註冊 Blob</span>
              <span class="text-sm font-medium text-slate-900">{{ progressState['walrus-register'] }}</span>
            </div>
            <div class="flex items-center justify-between rounded-2xl border border-slate-200 px-4 py-3">
              <span class="text-sm text-slate-700">Walrus 上傳資料</span>
              <span class="text-sm font-medium text-slate-900">{{ progressState['walrus-upload'] }}</span>
            </div>
            <div class="flex items-center justify-between rounded-2xl border border-slate-200 px-4 py-3">
              <span class="text-sm text-slate-700">Walrus 認證 Blob</span>
              <span class="text-sm font-medium text-slate-900">{{ progressState['walrus-certify'] }}</span>
            </div>
            <div class="flex items-center justify-between rounded-2xl border border-slate-200 px-4 py-3">
              <span class="text-sm text-slate-700">鏈上建立 EncFileRecord</span>
              <span class="text-sm font-medium text-slate-900">{{ progressState.chain }}</span>
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
          <div v-else-if="!uploads.length" class="mt-4 rounded-2xl border border-dashed border-slate-300 px-4 py-5 text-sm text-slate-500">
            目前還沒有 mediSciNet 上傳紀錄。
          </div>
          <div v-else class="mt-4 space-y-3">
            <div v-for="upload in uploads" :key="upload.id" class="rounded-2xl border border-slate-200 p-4">
              <div class="flex items-start justify-between gap-3">
                <div>
                  <p class="font-medium text-slate-900">{{ upload.date_range_start }} ~ {{ upload.date_range_end }}</p>
                  <p class="mt-1 text-sm text-slate-500">{{ upload.record_count }} 筆 · {{ upload.file_size_bytes }} bytes</p>
                </div>
                <span class="rounded-full px-3 py-1 text-xs font-medium"
                  :class="upload.status === 'confirmed' ? 'bg-emerald-100 text-emerald-700' : upload.status === 'expired' ? 'bg-amber-100 text-amber-700' : 'bg-blue-100 text-blue-700'">
                  {{ formatStatus(upload.status) }}
                </span>
              </div>

              <p v-if="upload.blob_id" class="mt-3 break-all font-mono text-xs text-slate-600">Blob: {{ upload.blob_id }}</p>
              <p v-if="upload.user_file_cap_id" class="mt-1 break-all font-mono text-xs text-slate-600">Cap: {{ upload.user_file_cap_id }}</p>
            </div>
          </div>
        </div>

        <div class="rounded-3xl border border-slate-200 bg-slate-900 p-6 text-slate-100 shadow-sm">
          <h2 class="text-lg font-semibold">本次上傳摘要</h2>
          <dl class="mt-4 space-y-3 text-sm">
            <div class="flex items-center justify-between gap-4">
              <dt class="text-slate-400">日期範圍</dt>
              <dd>{{ startDate || '-' }} ~ {{ endDate || '-' }}</dd>
            </div>
            <div class="flex items-center justify-between gap-4">
              <dt class="text-slate-400">紀錄數</dt>
              <dd>{{ preview?.record_count ?? '-' }}</dd>
            </div>
            <div class="flex items-center justify-between gap-4">
              <dt class="text-slate-400">保留 Epoch</dt>
              <dd>{{ epochs }}</dd>
            </div>
            <div class="flex items-center justify-between gap-4">
              <dt class="text-slate-400">預估上傳費</dt>
              <dd>{{ feeEstimateSui }} SUI</dd>
            </div>
            <div class="flex items-center justify-between gap-4">
              <dt class="text-slate-400">目標 End Epoch</dt>
              <dd>{{ walrusEndEpoch || '-' }}</dd>
            </div>
          </dl>
        </div>
      </aside>
    </div>
  </div>
</template>
