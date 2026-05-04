import { Ed25519Keypair } from '@mysten/sui/keypairs/ed25519';
import { decodeSuiPrivateKey } from '@mysten/sui/cryptography';
import { SuiClient } from '@mysten/sui/client';
import { MedeSciNetDevClient } from '../../../medeSciNet-monorepo/packages/dev_sdk/dist/index.js';

const JSON_RPC_URLS = {
  mainnet: 'https://fullnode.mainnet.sui.io',
  testnet: 'https://fullnode.testnet.sui.io',
  devnet: 'https://fullnode.devnet.sui.io',
};

function decodePrivateKey(privateKey) {
  const normalized = String(privateKey || '').trim();
  if (!normalized) {
    throw new Error('PRIVATE_KEY is required');
  }

  if (normalized.startsWith('suiprivkey')) {
    return decodeSuiPrivateKey(normalized).secretKey;
  }

  const raw = Buffer.from(normalized, 'base64');
  if (raw.length === 32) {
    return new Uint8Array(raw);
  }
  if (raw.length === 33) {
    return new Uint8Array(raw.slice(1));
  }

  throw new Error('Unsupported PRIVATE_KEY format. Use suiprivkey... or base64-encoded ed25519 bytes.');
}

function buildSigner(privateKey, network) {
  const secretKey = decodePrivateKey(privateKey);
  const keypair = Ed25519Keypair.fromSecretKey(secretKey);
  const client = new SuiClient({
    url: JSON_RPC_URLS[network] || JSON_RPC_URLS.testnet,
  });

  return {
    address: keypair.toSuiAddress(),
    async signAndExecuteTransaction(tx) {
      const result = await client.signAndExecuteTransaction({
        transaction: tx,
        signer: keypair,
        options: {
          showEffects: true,
        },
      });

      if (result?.$kind === 'FailedTransaction') {
        throw new Error(result.FailedTransaction?.status?.error?.message || 'Transaction failed');
      }

      return {
        digest: result?.Transaction?.digest || result?.digest || '',
        effects: result?.Transaction?.effects || result?.effects,
      };
    },
    async signPersonalMessage(message) {
      const { signature } = await keypair.signPersonalMessage(message);
      return signature;
    },
  };
}

export function createIssuerClient(config) {
  const signer = buildSigner(config.privateKey, config.network);
  return new MedeSciNetDevClient({
    network: config.network,
    registryId: config.registryId,
    devCapId: config.devCapId,
    signer,
  });
}
