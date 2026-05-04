import { defineStore } from 'pinia'
import axios from 'axios'
import { getWallets } from '@mysten/wallet-standard'

axios.defaults.baseURL = 'http://localhost:8000'

const EMPTY_ADDRESS = null

export const useWalletStore = defineStore('wallet', {
  state: () => ({
    wallets: [],
    connectedWallet: null,
    address: EMPTY_ADDRESS,
    isConnecting: false,
    error: null,
    initialized: false,
    _unsubscribeWalletEvents: null,
  }),

  actions: {
    initialize() {
      if (this.initialized) {
        return
      }

      this.initialized = true
      this.refreshWallets()

      const walletsApi = getWallets()
      if (walletsApi && typeof walletsApi.on === 'function') {
        this._unsubscribeWalletEvents = walletsApi.on('change', () => {
          this.refreshWallets()
        })
      }
    },

    refreshWallets() {
      const walletsApi = getWallets()
      this.wallets = typeof walletsApi.get === 'function' ? walletsApi.get() : []

      const connectedWallet = this.wallets.find(wallet => Array.isArray(wallet.accounts) && wallet.accounts.length > 0) || null
      this.connectedWallet = connectedWallet
      this.address = connectedWallet?.accounts?.[0]?.address ?? EMPTY_ADDRESS
    },

    async connect(wallet) {
      const targetWallet = wallet || this.wallets[0]
      if (!targetWallet) {
        this.error = '找不到可用的 Sui 錢包'
        return null
      }

      const connectFeature = targetWallet.features?.['standard:connect']
      if (!connectFeature || typeof connectFeature.connect !== 'function') {
        this.error = '此錢包不支援標準連接功能'
        return null
      }

      this.isConnecting = true
      this.error = null

      try {
        await connectFeature.connect()
        this.refreshWallets()
        return this.connectedWallet
      } catch (error) {
        this.error = error?.message || '錢包連接失敗'
        return null
      } finally {
        this.isConnecting = false
      }
    },

    async disconnect() {
      const targetWallet = this.connectedWallet
      if (!targetWallet) {
        return
      }

      const disconnectFeature = targetWallet.features?.['standard:disconnect']
      if (disconnectFeature && typeof disconnectFeature.disconnect === 'function') {
        try {
          await disconnectFeature.disconnect()
        } catch (error) {
          this.error = error?.message || '錢包斷開失敗'
        }
      }

      this.connectedWallet = null
      this.address = EMPTY_ADDRESS
    },

    async signMessage(message) {
      const wallet = this.connectedWallet
      const account = wallet?.accounts?.[0]
      if (!wallet || !account) {
        throw new Error('請先連接錢包')
      }

      const signFeature = wallet.features?.['sui:signPersonalMessage'] || wallet.features?.['sui:signMessage']
      if (!signFeature || typeof signFeature.signPersonalMessage !== 'function') {
        throw new Error('此錢包不支援個人訊息簽章')
      }

      const messageBytes = typeof message === 'string' ? new TextEncoder().encode(message) : message
      const result = await signFeature.signPersonalMessage({ account, message: messageBytes })

      return {
        signature: result.signature,
        publicKey: account.publicKey,
        bytes: result.bytes,
      }
    },

    async signAndExecuteTransaction(txb) {
      const wallet = this.connectedWallet
      const account = wallet?.accounts?.[0]
      if (!wallet || !account) {
        throw new Error('請先連接錢包')
      }

      const executeFeature = wallet.features?.['sui:signAndExecuteTransaction'] || wallet.features?.['sui:signAndExecuteTransactionBlock']
      if (!executeFeature) {
        throw new Error('此錢包不支援交易簽章')
      }

      if (typeof executeFeature.signAndExecuteTransaction === 'function') {
        return executeFeature.signAndExecuteTransaction({ account, transaction: txb })
      }

      if (typeof executeFeature.signAndExecuteTransactionBlock === 'function') {
        return executeFeature.signAndExecuteTransactionBlock({ account, transactionBlock: txb })
      }

      throw new Error('此錢包不支援交易執行')
    },
  },
})