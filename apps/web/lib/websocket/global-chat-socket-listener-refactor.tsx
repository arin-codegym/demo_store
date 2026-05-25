'use client';

import { useEffect, useMemo } from 'react';
import type { IMessage } from '@stomp/stompjs';
import {
  InfiniteData,
  QueryClient,
  useQueryClient,
} from '@tanstack/react-query';
import { usePathname } from 'next/navigation';
import { useCurrentUser } from '@/query/auth/useCurrentUser';
import {
  connectStomp,
  syncSubscriptions,
  unsubscribeConversation,
  unsubscribeSidebar,
} from '@/lib/websocket/chat-socket-refactor';
import { queryKeys } from '@/query/query-keys';
import type {
  ConversationSeenPayload,
  ConversationUpdatedPayload,
  Message,
  MessageCreatedPayload,
  NotificationCreatedPayload,
  NotificationReadAllPayload,
  NotificationReadPayload,
  WsEnvelope,
} from '@/utils/types';
import { getMe } from '@/lib/api/get-me';
import { useChatUiReduxSelector } from '@/stores/chat-ui-redux-store';
import {
  decreaseUnreadNotificationCount,
  increaseUnreadNotificationCount,
  markAllNotificationsReadInCache,
  markNotificationReadInCache,
  prependNotificationToCache,
} from '@/lib/realtime/notification-cache';
import { toast } from '@/components/ui/use-toast';
import { markReadByConversationId } from '@/lib/api/notifications';

export function GlobalChatSocketListenerRefactor() {
  const pathname = usePathname();
  const queryClient = useQueryClient();
  const { data: me } = useCurrentUser();

  // Zustand: useChatUiStore((s) => s.stompConnected)
  const stompConnected = useChatUiReduxSelector(
    (state) => state.chatUi.stompConnected,
  );
  // Zustand: useChatUiStore((s) => s.selectedConversationId)
  const selectedConversationId = useChatUiReduxSelector(
    (state) => state.chatUi.selectedConversationId,
  );
  // Zustand: useChatUiStore((s) => s.adminConversationId)
  const adminConversationId = useChatUiReduxSelector(
    (state) => state.chatUi.adminConversationId,
  );
  // Zustand: useChatUiStore((s) => s.adminWidgetOpen)
  const adminWidgetOpen = useChatUiReduxSelector(
    (state) => state.chatUi.adminWidgetOpen,
  );

  // Zustand: useChatUiStore((s) => s.aiConversationId)
  const aiConversationId = useChatUiReduxSelector(
    (state) => state.chatUi.aiConversationId,
  );
  // Zustand: useChatUiStore((s) => s.aiWidgetOpen)
  const aiWidgetOpen = useChatUiReduxSelector(
    (state) => state.chatUi.aiWidgetOpen,
  );
  // Zustand: useChatUiStore((s) => s.conversationWidgetConversationId)
  const conversationWidgetConversationId = useChatUiReduxSelector(
    (state) => state.chatUi.conversationWidgetConversationId,
  );
  // Zustand: useChatUiStore((s) => s.conversationWidgetOpen)
  const conversationWidgetOpen = useChatUiReduxSelector(
    (state) => state.chatUi.conversationWidgetOpen,
  );
  // Only one conversation context can be considered active for read/unread
  // decisions, whether it is the full chat page or one of the floating widgets.
  const activeConversationId = useMemo(() => {
    const isChatPage = pathname.startsWith('/chat');

    if (isChatPage) return selectedConversationId;

    if (conversationWidgetOpen) return conversationWidgetConversationId;

    if (adminWidgetOpen) return adminConversationId;

    if (aiWidgetOpen) return aiConversationId;

    return null;
  }, [
    pathname,
    selectedConversationId,
    conversationWidgetOpen,
    conversationWidgetConversationId,
    adminConversationId,
    adminWidgetOpen,
    aiConversationId,
    aiWidgetOpen,
  ]);

  useEffect(() => {
    if (!me) return;

    let cancelled = false;

    const boot = async () => {
      try {
        await ensureAuthSession(queryClient);
        if (cancelled) return;

        connectStomp();
      } catch (error) {
        console.error('[socket] Cannot connect stomp', error);
      }
    };

    boot();

    return () => {
      cancelled = true;
    };
  }, [me, queryClient]);

  useEffect(() => {
    if (!me?.userId) {
      unsubscribeSidebar();
      unsubscribeConversation();
      return;
    }

    const onSocketMessage = function (frame: IMessage): void {
      handleSocketMessage({
        frame: frame,
        meUserId: me.userId,
        activeConversationId: activeConversationId,
        queryClient: queryClient,
      });
    };

    syncSubscriptions({
      stompConnected,
      userId: me.userId,
      activeConversationId,
      onMessage: onSocketMessage,
    });

    return () => {
      unsubscribeSidebar();
      unsubscribeConversation();
    };
  }, [stompConnected, me?.userId, activeConversationId, queryClient]);

  return null;
}

type HandleSocketMessageParams = {
  frame: IMessage;
  meUserId?: string;
  activeConversationId: string | null;
  queryClient: QueryClient;
};

function handleSocketMessage({
  frame,
  meUserId,
  activeConversationId,
  queryClient,
}: HandleSocketMessageParams) {
  try {
    const envelope = JSON.parse(frame.body) as WsEnvelope;

    switch (envelope.type) {
      case 'message.created':
        handleMessageCreated(
          envelope.data as MessageCreatedPayload,
          queryClient,
          activeConversationId,
        );
        return;

      case 'conversation.updated':
        handleConversationUpdated(
          envelope.data as ConversationUpdatedPayload,
          queryClient,
        );
        return;

      case 'conversation.seen':
        handleConversationSeen(
          envelope.data as ConversationSeenPayload,
          queryClient,
          meUserId,
        );
        return;

      case 'notification.created':
        handleNotificationCreated(
          envelope.data as NotificationCreatedPayload,
          queryClient,
          activeConversationId,
        );
        return;

      case 'notification.read':
        handleNotificationRead(
          envelope.data as NotificationReadPayload,
          queryClient,
        );
        return;

      case 'notification.read-all':
        handleNotificationReadAll(
          envelope.data as NotificationReadAllPayload,
          queryClient,
        );
        return;

      default:
        return;
    }
  } catch (error) {
    console.error('Invalid ws payload', error, frame.body);
  }
}
function handleMessageCreated(
  payload: MessageCreatedPayload,
  queryClient: QueryClient,
  activeConversationId: string | null,
) {
  const queryKey = queryKeys.infiniteMessages(payload.conversationId);

  // Show socket messages immediately while a background refetch reconciles with
  // backend state.
  queryClient.setQueryData<InfiniteData<Message[]> | undefined>(
    queryKey,
    (old) => upsertMessageInInfiniteCache(old, payload),
  );

  queryClient.invalidateQueries({
    queryKey: queryKeys.infiniteMessages(payload.conversationId),
    refetchType: 'active',
  });

  queryClient.invalidateQueries({
    queryKey: queryKeys.conversations,
  });

  const isActiveConversation = payload.conversationId === activeConversationId;

  if (isActiveConversation) {
    return;
  }
}

function handleConversationUpdated(
  payload: ConversationUpdatedPayload,
  queryClient: QueryClient,
) {
  queryClient.invalidateQueries({
    queryKey: queryKeys.conversations,
  });

  queryClient.invalidateQueries({
    queryKey: queryKeys.infiniteMessages(payload.conversationId),
  });
}

function handleConversationSeen(
  payload: ConversationSeenPayload,
  queryClient: QueryClient,
  meUserId?: string,
) {
  if (payload.userId !== meUserId) {
    queryClient.setQueryData(
      queryKeys.conversationReadState(payload.conversationId),
      {
        otherUserLastReadMessageId: payload.lastReadMessageId,
        seenUserId: payload.userId,
      },
    );
  }

  queryClient.invalidateQueries({
    queryKey: queryKeys.conversations,
  });
}

function handleNotificationCreated(
  payload: NotificationCreatedPayload,
  queryClient: QueryClient,
  activeConversationId: string | null,
) {
  const notification = payload.notification;
  const conversationId = notification.target?.id;

  const isActiveChatNotification =
    notification.type === 'chat.message.created' &&
    conversationId &&
    conversationId === activeConversationId;

  if (isActiveChatNotification) {
    // The user is already viewing this conversation, so persist it as read on
    // the backend instead of briefly increasing the unread badge locally.
    markReadByConversationId(conversationId)
      .then(async (result) => {
        await queryClient.invalidateQueries({
          queryKey: queryKeys.unreadNotificationCount(),
          exact: true,
        });

        await queryClient.invalidateQueries({
          queryKey: queryKeys.infiniteNotifications(),
        });

        await queryClient.invalidateQueries({
          queryKey: queryKeys.conversations,
        });
      })
      .catch((error) => {
        console.error('[ws] Failed to mark notification as read', error);
      });

    return;
  }

  prependNotificationToCache(queryClient, payload.notification);
  increaseUnreadNotificationCount(queryClient);

  if (shouldShowToast(payload.notification)) {
    toast({
      title: payload.notification.title,
      description: payload.notification.body,
    });
  }
}

function handleNotificationRead(
  payload: NotificationReadPayload,
  queryClient: QueryClient,
) {
  markNotificationReadInCache(
    queryClient,
    payload.notificationId,
    payload.readAt,
  );

  decreaseUnreadNotificationCount(queryClient);
}

function handleNotificationReadAll(
  payload: NotificationReadAllPayload,
  queryClient: QueryClient,
) {
  markAllNotificationsReadInCache(queryClient, payload.readAt);
}

function shouldShowToast(
  notification: NotificationCreatedPayload['notification'],
) {
  return notification.type === 'chat.message.created';
}

async function ensureAuthSession(queryClient: QueryClient) {
  await queryClient.fetchQuery({
    queryKey: queryKeys.me,
    queryFn: getMe,
    staleTime: 0,
  });
}

function upsertMessageInInfiniteCache(
  old: InfiniteData<Message[]> | undefined,
  incoming: Message,
): InfiniteData<Message[]> {
  if (!old) {
    return {
      pages: [[incoming]],
      pageParams: [undefined],
    };
  }

  const exists = old.pages.some((page) =>
    page.some((message) => message.messageId === incoming.messageId),
  );

  // Replace a temporary optimistic message with the backend-confirmed version.
  if (exists) {
    return {
      ...old,
      pages: old.pages.map((page) =>
        page.map((message) =>
          message.messageId === incoming.messageId
            ? {
                ...message,
                ...incoming,
                isTemp: false,
              }
            : message,
        ),
      ),
    };
  }

  const pages = [...old.pages];

  if (pages.length === 0) {
    return {
      ...old,
      pages: [[incoming]],
    };
  }

  // The first page is the newest page rendered by the chat view.
  pages[0] = [...(pages[0] ?? []), { ...incoming, isTemp: false }];

  return {
    ...old,
    pages,
  };
}
