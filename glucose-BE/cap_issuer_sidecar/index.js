import 'dotenv/config';
import express from 'express';
import { createIssuerClient } from './issuer.js';

const app = express();
app.use(express.json());

const port = Number(process.env.PORT || '7777');

app.get('/health', (_req, res) => {
  res.json({
    ok: true,
    port,
    network: process.env.NETWORK || 'testnet',
  });
});

/**
 * Issue a UserFileCap and create a SubscriptionState for the user in one PTB.
 * Body: { user_wallet_address, walrus_end_epoch }
 * Response: { cap_object_id, sub_state_id, tx_digest, walrus_end_epoch }
 */
app.post('/issue-cap', async (req, res) => {
  const { user_wallet_address: userWalletAddress, walrus_end_epoch: walrusEndEpoch } = req.body || {};

  if (!userWalletAddress || !walrusEndEpoch) {
    return res.status(400).json({ error: 'user_wallet_address and walrus_end_epoch are required' });
  }

  try {
    const client = createIssuerClient({
      network: process.env.NETWORK || 'testnet',
      privateKey: process.env.PRIVATE_KEY || '',
      registryId: process.env.DEV_REGISTRY_ID || '',
      devCapId: process.env.DEV_CAP_ID || '',
    });

    const result = await client.issueUserFileCapWithSubscription(
      userWalletAddress,
      BigInt(walrusEndEpoch),
    );

    return res.json({
      cap_object_id: result.capObjectId,
      sub_state_id: result.subStateObjectId,
      tx_digest: result.txDigest,
      walrus_end_epoch: Number(walrusEndEpoch),
    });
  } catch (error) {
    return res.status(500).json({
      error: error instanceof Error ? error.message : 'Unknown sidecar error',
    });
  }
});

/**
 * Settle service fee from user vault using a ServiceSettlementApproval.
 * Body: { vault_id, approval_id, sub_state_id, epochs_to_settle }
 * Response: { ok, tx_digest, new_service_until }
 */
app.post('/settle-service-fees', async (req, res) => {
  const {
    vault_id: vaultId,
    approval_id: approvalId,
    sub_state_id: subStateId,
    epochs_to_settle: epochsToSettle,
  } = req.body || {};

  if (!vaultId || !approvalId || !subStateId || !epochsToSettle) {
    return res.status(400).json({
      error: 'vault_id, approval_id, sub_state_id, and epochs_to_settle are required',
    });
  }

  try {
    const client = createIssuerClient({
      network: process.env.NETWORK || 'testnet',
      privateKey: process.env.PRIVATE_KEY || '',
      registryId: process.env.DEV_REGISTRY_ID || '',
      devCapId: process.env.DEV_CAP_ID || '',
    });

    const result = await client.settleServiceFeeWithApproval(
      vaultId,
      approvalId,
      subStateId,
      Number(epochsToSettle),
    );

    return res.json({
      ok: true,
      tx_digest: result.txDigest,
      new_service_until: result.newServiceUntil,
    });
  } catch (error) {
    return res.status(500).json({
      error: error instanceof Error ? error.message : 'Unknown sidecar error',
    });
  }
});

/**
 * Sync on-chain subscription state and vault info for a user.
 * Body: { vault_id, user_wallet }
 * Response: { service_active_until_epoch, projected_service_end_epoch, vault_balance_mist, service_credit_mist }
 */
app.post('/sync-subscription-state', async (req, res) => {
  const {
    vault_id: vaultId,
    user_wallet: userWallet,
  } = req.body || {};

  if (!vaultId || !userWallet) {
    return res.status(400).json({
      error: 'vault_id and user_wallet are required',
    });
  }

  try {
    const client = createIssuerClient({
      network: process.env.NETWORK || 'testnet',
      privateKey: process.env.PRIVATE_KEY || '',
      registryId: process.env.DEV_REGISTRY_ID || '',
      devCapId: process.env.DEV_CAP_ID || '',
    });

    const info = await client.getProjectedServiceInfo(vaultId, userWallet);

    return res.json({
      service_active_until_epoch: Number(info.serviceActiveUntilEpoch),
      projected_service_end_epoch: Number(info.projectedServiceEndEpoch),
      vault_balance_mist: info.vaultResidualMist.toString(),
      service_credit_mist: info.serviceCreditMist.toString(),
    });
  } catch (error) {
    return res.status(500).json({
      error: error instanceof Error ? error.message : 'Unknown sidecar error',
    });
  }
});

app.listen(port, () => {
  console.log(`cap-issuer-sidecar listening on :${port}`);
});
