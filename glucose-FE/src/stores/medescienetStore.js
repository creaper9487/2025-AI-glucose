import { defineStore } from 'pinia';
import axiosInstance from '@/components/axiosInstance';

export const useMedescienetStore = defineStore('medescienet', {
  state: () => ({
    subscriptionStatus: 'active', // active, expiring_soon, expired
    vaultBalance: 0,
    serviceCreditBalance: 0,
    projectedServiceEndEpoch: 0,
    settlementApprovalId: null,
    uploadHistory: []
  }),

  actions: {
    async fetchSubscriptionStatus() {
      try {
        const response = await axiosInstance.get('/api/medescienet/subscription/');
        const data = response.data;
        this.subscriptionStatus = data.status || 'expired';
        this.vaultBalance = data.vault_balance_mist || 0;
        this.serviceCreditBalance = data.service_credit_mist || 0;
        this.projectedServiceEndEpoch = data.projected_service_end_epoch || 0;
        this.settlementApprovalId = data.settlement_approval_id || null;
        return data;
      } catch (error) {
        console.error('Error fetching subscription status:', error);
        throw error;
      }
    },

    async requestUploadCap() {
      try {
        const response = await axiosInstance.post('/api/medescienet/request-upload-cap/');
        return response.data;
      } catch (error) {
        console.error('Error requesting upload cap:', error);
        throw error;
      }
    },

    async approveSettlement(approvalId, amountMist, maxEpochsPerSettlement) {
      try {
        const response = await axiosInstance.post('/api/medescienet/subscription/approve-settlement/', {
          approval_object_id: approvalId,
          amount_mist: amountMist,
          max_epochs_per_settlement: maxEpochsPerSettlement
        });
        this.settlementApprovalId = approvalId;
        return response.data;
      } catch (error) {
        console.error('Error approving settlement:', error);
        throw error;
      }
    },

    async syncOnChainState() {
      try {
        const response = await axiosInstance.post('/api/medescienet/sync-subscription/');
        const data = response.data;
        this.vaultBalance = data.vault_balance_mist || 0;
        this.serviceCreditBalance = data.service_credit_mist || 0;
        this.projectedServiceEndEpoch = data.projected_service_end_epoch || 0;
        return data;
      } catch (error) {
        console.error('Error syncing on chain state:', error);
        throw error;
      }
    },

    setVaultId(vaultId) {
      // Typically we'll want to maybe re-sync or just hold it
      this.currentVaultId = vaultId;
    }
  }
});
