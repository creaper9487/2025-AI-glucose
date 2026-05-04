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

    const result = await client.issueUserFileCapWithResult(userWalletAddress, BigInt(walrusEndEpoch));
    return res.json({
      cap_object_id: result.objectId,
      tx_digest: result.digest,
      walrus_end_epoch: Number(walrusEndEpoch),
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
