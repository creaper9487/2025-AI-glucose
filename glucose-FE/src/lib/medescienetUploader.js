import { SuiGrpcClient } from '@mysten/sui/grpc'
import { SealClient } from '@mysten/seal'
import { WalrusClient } from '@mysten/walrus'
import { CONTRACT_ADDRESSES, GRPC_URLS, SEAL_TESTNET_SERVER_CONFIGS, buildEncryptedUploadTx } from '../../../../medeSciNet-monorepo/packages/sdk/dist/index.js'
import { MedeSciNetUserClient } from '../../../../medeSciNet-monorepo/packages/dev_sdk/dist/index.js'

const NETWORK = import.meta.env.VITE_MEDESCIENET_NETWORK || 'testnet'
const PACKAGE_ID = import.meta.env.VITE_MEDESCIENET_PACKAGE_ID || CONTRACT_ADDRESSES[NETWORK]?.PACKAGE || CONTRACT_ADDRESSES.testnet.PACKAGE
const DEV_REGISTRY_ID = import.meta.env.VITE_MEDESCIENET_DEV_REGISTRY_ID || ''
const FILE_SIZE_UNIT_BYTES = BigInt(import.meta.env.VITE_MEDESCIENET_FILE_SIZE_UNIT_BYTES || '1048576')
const UPLOAD_FEE_PER_EPOCH = BigInt(import.meta.env.VITE_MEDESCIENET_UPLOAD_FEE_PER_EPOCH || '1000000')
const SEAL_THRESHOLD = Number(import.meta.env.VITE_MEDESCIENET_SEAL_THRESHOLD || '2')

function decodeBase64(base64Value) {
  const binary = atob(base64Value)
  const bytes = new Uint8Array(binary.length)
  for (let index = 0; index < binary.length; index += 1) {
    bytes[index] = binary.charCodeAt(index)
  }
  return bytes
}

function hexToBytes(hexValue) {
  const clean = hexValue.replace(/^0x/, '')
  if (!clean) return new Uint8Array()
  const pairs = clean.match(/.{1,2}/g) || []
  return new Uint8Array(pairs.map(pair => Number.parseInt(pair, 16)))
}

function extractDigest(result) {
  return result?.Transaction?.digest || result?.digest || ''
}

function extractErrorMessage(error) {
  if (error?.response?.data?.error) return error.response.data.error
  if (error?.message) return error.message
  return 'Unknown upload error'
}

async function sha256Hex(input) {
  const bytes = new TextEncoder().encode(input)
  const digest = await crypto.subtle.digest('SHA-256', bytes)
  return Array.from(new Uint8Array(digest))
    .map(byte => byte.toString(16).padStart(2, '0'))
    .join('')
}

function createWalletSigner(walletStore) {
  return {
    address: walletStore.address,
    async signAndExecuteTransaction(tx) {
      const result = await walletStore.signAndExecuteTransaction(tx)
      return {
        digest: extractDigest(result),
        effects: result?.Transaction?.effects || result?.effects,
      }
    },
    async signPersonalMessage(message) {
      const result = await walletStore.signMessage(message)
      return result.signature
    },
  }
}

export function estimateUploadFee(fileSizeBytes, epochs) {
  const units = BigInt(Math.max(1, Math.ceil(fileSizeBytes / Number(FILE_SIZE_UNIT_BYTES))))
  return UPLOAD_FEE_PER_EPOCH * BigInt(epochs) * units
}

export async function fetchCurrentWalrusEpoch() {
  const sui = new SuiGrpcClient({
    network: NETWORK,
    baseUrl: GRPC_URLS[NETWORK] || GRPC_URLS.testnet,
  })
  const walrus = new WalrusClient({ network: NETWORK, suiClient: sui })
  const state = await walrus.stakingState()
  return Number(state?.epoch || 0)
}

export async function uploadGlucoseData({
  walletStore,
  axiosInstance,
  rawDataBase64,
  capObjectId,
  walrusEpochs,
  uploadId,
  checksumSha256,
  onProgress,
}) {
  if (!walletStore?.address) {
    throw new Error('請先連接錢包')
  }
  if (!DEV_REGISTRY_ID) {
    throw new Error('缺少 VITE_MEDESCIENET_DEV_REGISTRY_ID 設定')
  }

  const plaintext = decodeBase64(rawDataBase64)
  const signer = createWalletSigner(walletStore)
  const userClient = new MedeSciNetUserClient({
    network: NETWORK,
    signer,
  })

  onProgress?.('vault', 'checking')
  let vaults = await userClient.getMyVaults(DEV_REGISTRY_ID)
  if (!vaults.length) {
    await userClient.createVault(DEV_REGISTRY_ID)
    vaults = await userClient.getMyVaults(DEV_REGISTRY_ID)
  }

  const vault = vaults[0]
  if (!vault) {
    throw new Error('無法建立或取得上傳 Vault')
  }

  const estimatedFeeMist = estimateUploadFee(plaintext.byteLength, walrusEpochs)
  if (vault.balance < estimatedFeeMist) {
    onProgress?.('vault', 'funding')
    await userClient.topUpVault(vault.objectId, estimatedFeeMist - vault.balance)
  }

  onProgress?.('seal', 'running')
  const sealHexId = await sha256Hex(`${capObjectId}:${checksumSha256 || ''}:${Date.now()}`)
  const sealFullId = `0x${sealHexId}`

  const sui = new SuiGrpcClient({
    network: NETWORK,
    baseUrl: GRPC_URLS[NETWORK] || GRPC_URLS.testnet,
  })
  const walrus = new WalrusClient({ network: NETWORK, suiClient: sui })
  const seal = new SealClient({
    suiClient: sui,
    serverConfigs: SEAL_TESTNET_SERVER_CONFIGS,
  })

  const { encryptedObject } = await seal.encrypt({
    threshold: SEAL_THRESHOLD,
    packageId: PACKAGE_ID,
    id: sealFullId,
    data: plaintext,
  })

  onProgress?.('walrus-register', 'running')
  const flow = walrus.writeBlobFlow({ blob: encryptedObject })
  await flow.encode()

  const registerTx = flow.register({
    epochs: walrusEpochs,
    owner: walletStore.address,
    deletable: true,
  })
  const registerResult = await walletStore.signAndExecuteTransaction(registerTx)
  const registerDigest = extractDigest(registerResult)

  onProgress?.('walrus-upload', 'running')
  await flow.upload({ digest: registerDigest })

  onProgress?.('walrus-certify', 'running')
  const certifyTx = flow.certify()
  const certifyResult = await walletStore.signAndExecuteTransaction(certifyTx)
  const certifyDigest = extractDigest(certifyResult)

  const blobState = await flow.getBlob()
  const blobId = blobState.blobId
  const walrusEndEpoch = Number(blobState.blobObject?.storage?.end_epoch || 0)

  onProgress?.('chain', 'running')
  const uploadTx = buildEncryptedUploadTx({
    network: NETWORK,
    userFileCapId: capObjectId,
    vaultId: vault.objectId,
    registryId: DEV_REGISTRY_ID,
    blobId: hexToBytes(blobId),
    sealId: hexToBytes(sealFullId),
    walrusEndEpoch: BigInt(walrusEndEpoch),
    blobWalrusEpochs: BigInt(walrusEpochs),
    fileSizeBytes: BigInt(plaintext.byteLength),
  })

  const chainResult = await walletStore.signAndExecuteTransaction(uploadTx)
  const txDigest = extractDigest(chainResult)

  if (axiosInstance && uploadId) {
    await axiosInstance.patch(`/api/medescienet/uploads/${uploadId}/`, {
      blob_id: blobId,
      seal_id: sealFullId,
      walrus_epoch: walrusEndEpoch,
      file_size_bytes: plaintext.byteLength,
      status: 'confirmed',
    })
  }

  onProgress?.('done', 'success')
  return {
    blobId,
    sealId: sealFullId,
    walrusEndEpoch,
    registerDigest,
    certifyDigest,
    txDigest,
    vaultId: vault.objectId,
  }
}

export { extractErrorMessage }
