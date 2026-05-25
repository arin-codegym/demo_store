'use client';

import { useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';

import {
  chatUiActions,
  useChatUiReduxDispatch,
  useChatUiReduxSelector,
} from '@/stores/chat-ui-redux-store';
import { AdminChatLauncher } from '@/components/chat/AdminChatLauncher';
import { useCurrentUser } from '@/query/auth/useCurrentUser';
import { ensureAdminConversation } from '@/lib/api/internal/conversations';
import { shouldHideGlobalChat } from './chat-route-visibility';

export function GlobalAdminChat() {
  const pathname = usePathname();
  const router = useRouter();
  const { data: user, isLoading } = useCurrentUser();

  const [isBootstrapping, setIsBootstrapping] = useState(false);

  const dispatch = useChatUiReduxDispatch();
  // Zustand: useChatUiStore((s) => s.adminConversationId)
  const adminConversationId = useChatUiReduxSelector(
    (state) => state.chatUi.adminConversationId,
  );
  // Zustand: useChatUiStore((s) => s.setAdminConversationId)
  const setAdminConversationId = (conversationId: string | null) =>
    dispatch(chatUiActions.setAdminConversationId(conversationId));
  // Zustand: useChatUiStore((s) => s.openConversationWidget)
  const openConversationWidget = (
    conversationId: string,
    type: 'USER_ADMIN',
  ) => dispatch(chatUiActions.openConversationWidget({ conversationId, type }));

  const shouldHideChat = shouldHideGlobalChat(pathname);

  if (shouldHideChat) return null;

  // Admin thì không cần nút "chat với admin"
  if (user?.roles?.includes('ROLE_ADMIN')) return null;

  const handleOpen = async () => {
    if (isLoading || isBootstrapping) return;

    if (!user) {
      router.push('/login?redirect=' + encodeURIComponent(pathname));
      return;
    }

    try {
      setIsBootstrapping(true);

      let id = adminConversationId;

      if (id == null) {
        id = await ensureAdminConversation();
        if (id == null) return;

        setAdminConversationId(id);
      }

      openConversationWidget(id, 'USER_ADMIN');
    } catch (error) {
      console.error('Failed to open admin conversation widget', error);
    } finally {
      setIsBootstrapping(false);
    }
  };

  return (
    <AdminChatLauncher
      onClick={handleOpen}
      disabled={isLoading || isBootstrapping}
    />
  );
}
