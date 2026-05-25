'use client';

import { useRouter } from 'next/navigation';
import { useQueryClient } from '@tanstack/react-query';
import { logoutAction } from '@/server/auth-action';
import { disconnectStomp } from '@/lib/websocket/chat-socket-refactor';
import {
  chatUiActions,
  chatUiReduxStore,
} from '@/stores/chat-ui-redux-store';

export function useLogout() {
  const router = useRouter();
  const queryClient = useQueryClient();

  async function logout() {
    try {
      await logoutAction();
    } finally {
      disconnectStomp();
      // Zustand: useChatUiStore.getState().reset()
      chatUiReduxStore.dispatch(chatUiActions.reset());
      queryClient.clear();
      router.replace('/login');
    }
  }

  return { logout };
}
