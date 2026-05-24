// 'use client';

import { loginClient } from '@/lib/auth.client';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';

export function useLogin() {
  const qc = useQueryClient();
  const router = useRouter();

  return useMutation({
    mutationFn: ({
      username,
      password,
    }: {
      username: string;
      password: string;
    }) => loginClient(username, password),

    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ['me'] });
      await qc.invalidateQueries({ queryKey: ['cart-count'] });
      router.push('/');
    },
    onError: (e) => {
      console.log('onError called', e);
    },
    onSettled: () => {
      // console.log('onSettled called');
    },
  });
}
