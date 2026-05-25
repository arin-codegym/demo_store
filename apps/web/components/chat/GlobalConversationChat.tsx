'use client';

import { useRef } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import {
  chatUiActions,
  useChatUiReduxDispatch,
  useChatUiReduxSelector,
} from '@/stores/chat-ui-redux-store';
import { AdminChatWidget } from '@/components/chat/AdminChatWidget';
import { useCurrentUser } from '@/query/auth/useCurrentUser';
import { shouldHideGlobalChat } from './chat-route-visibility';

function mapConversationTypeToQueryType(
  type: 'USER_ADMIN' | 'DIRECT' | 'USER_DIRECT' | null,
) {
  switch (type) {
    case 'USER_ADMIN':
      return 'admin';
    case 'DIRECT':
    case 'USER_DIRECT':
      return 'direct';
    default:
      return 'conversation';
  }
}

export function GlobalConversationChat() {
  const pathname = usePathname();
  const router = useRouter();
  const { data: user } = useCurrentUser();

  const dispatch = useChatUiReduxDispatch();
  // Zustand: useChatUiStore((s) => s.conversationWidgetOpen)
  const conversationWidgetOpen = useChatUiReduxSelector(
    (state) => state.chatUi.conversationWidgetOpen,
  );
  // Zustand: useChatUiStore((s) => s.conversationWidgetConversationId)
  const conversationWidgetConversationId = useChatUiReduxSelector(
    (state) => state.chatUi.conversationWidgetConversationId,
  );
  // Zustand: useChatUiStore((s) => s.conversationWidgetType)
  const conversationWidgetType = useChatUiReduxSelector(
    (state) => state.chatUi.conversationWidgetType,
  );
  // Zustand: useChatUiStore((s) => s.closeConversationWidget)
  const closeConversationWidget = () =>
    dispatch(chatUiActions.closeConversationWidget());

  const lastMarkedKeyRef = useRef<string | null>(null);

  if (shouldHideGlobalChat(pathname)) return null;

  if (!conversationWidgetOpen || !conversationWidgetConversationId) {
    return null;
  }

  const handleClose = () => {
    closeConversationWidget();
    lastMarkedKeyRef.current = null;
  };

  const handleOpenInMain = () => {
    const queryType = mapConversationTypeToQueryType(conversationWidgetType);

    // Transfer the floating widget state into the full chat page route.
    closeConversationWidget();
    lastMarkedKeyRef.current = null;

    router.push(
      `/chat?conversationId=${conversationWidgetConversationId}&type=${queryType}`,
    );
  };

  return (
    <AdminChatWidget
      conversationId={conversationWidgetConversationId}
      onClose={handleClose}
      onOpenInMain={handleOpenInMain}
      currentUserId={user?.userId}
    />
  );
}
