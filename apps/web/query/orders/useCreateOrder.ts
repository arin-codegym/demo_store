// hooks/useCreateOrder.ts
'use client';

import { useMutation } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';

export function useCreateOrder() {
  const router = useRouter();

  return useMutation({
    mutationFn: async () => {
      const key =
        globalThis.crypto?.randomUUID?.() ??
        `${Date.now()}-${Math.random().toString(16).slice(2)}`;
      const res = await fetch('/api/order/create', {
        method: 'POST',
        headers: {
          'Idempotency-Key': key,
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
