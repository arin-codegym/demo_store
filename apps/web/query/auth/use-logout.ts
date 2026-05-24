'use client';

import { useRouter } from 'next/navigation';
import { useQueryClient } from '@tanstack/react-query';
import { logoutAction } from '@/server/auth-action';
import { disconnectStomp } from '@/lib/websocket/chat-socket-refactor';
import { useChatUiStore } from '@/stores/chat-ui-store';

export function useLogout() {
  const router = useRouter();
  const queryClient = useQueryClient();

  async function logout() {
    try {
      await logoutAction();
    } finally {
      disconnectStomp();
      useChatUiStore.getState().reset();
      queryClient.clear();
      router.replace('/login');
    }
  }

  return { logout };
}
