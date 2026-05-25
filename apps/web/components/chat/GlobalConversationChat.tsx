'use client';

import { useRef } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { useChatUiStore } from '@/stores/chat-ui-store';
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

  const conversationWidgetOpen = useChatUiStore(
    (s) => s.conversationWidgetOpen,
  );
  const conversationWidgetConversationId = useChatUiStore(
    (s) => s.conversationWidgetConversationId,
  );
  const conversationWidgetType = useChatUiStore(
    (s) => s.conversationWidgetType,
  );
  const closeConversationWidget = useChatUiStore(
    (s) => s.closeConversationWidget,
  );

  const lastMarkedKeyRef = useRef<string | null>(null);

  // useEffect(() => {
  //   if (
  //     !conversationWidgetOpen ||
  //     !conversationWidgetConversationId ||
  //     !conversationWidgetType
  //   ) {
  //     return;
  //   }

  //   const markKey = `${conversationWidgetConversationId}:${conversationWidgetType}`;
  //   if (lastMarkedKeyRef.current === markKey) return;

  //   lastMarkedKeyRef.current = markKey;

  //   markReadByConversation(conversationWidgetConversationId);
  // }, [
  //   conversationWidgetOpen,
  //   conversationWidgetConversationId,
  //   conversationWidgetType,
  //   markReadByConversation,
  // ]);

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
