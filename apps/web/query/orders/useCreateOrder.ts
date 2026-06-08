// hooks/useCreateOrder.ts
'use client';

import { useMutation } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { useRef } from 'react';

function createIdempotencyKey() {
  return (
    globalThis.crypto?.randomUUID?.() ??
    `${Date.now()}-${Math.random().toString(16).slice(2)}`
  );
}

export function useCreateOrder() {
  const router = useRouter();
  const idempotencyKeyRef = useRef<string | null>(null);

  const idempotencyKey = idempotencyKeyRef.current ?? createIdempotencyKey();
  idempotencyKeyRef.current = idempotencyKey;

  return useMutation({
    mutationFn: async () => {
      const res = await fetch('/api/order/create', {
        method: 'POST',
        headers: {
          'Idempotency-Key': idempotencyKey,
        },
        credentials: 'include',
        // body: JSON.stringify({
        //   cartId: cartId,
        // }),
      });
      const text = await res.text();

      if (!res.ok) {
        let message = 'Failed';
        try {
          const json = JSON.parse(text);
          message = json.message || json.error || text || 'Failed';
        } catch {
          message = text || 'Failed';
        }
        throw new Error(message);
      }

      return JSON.parse(text);
    },

    onSuccess: (data) => {
      router.push(`/checkout?orderId=${data.orderId}&cartId=${data.cartId}`);
    },
    onError: (err) => {
      console.error('create order failed:', err);
      alert(err instanceof Error ? err.message : 'Create order failed');
    },
  });
}
