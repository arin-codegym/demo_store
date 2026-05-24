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
  MessageHandler,
  subscribeConversation,
  subscribeSidebar,
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
import { useChatUiStore } from '@/stores/chat-ui-store';
import {
  decreaseUnreadNotificationCount,
  increaseUnreadNotificationCount,
  markAllNotificationsReadInCache,
  markNotificationReadInCache,
  prependNotificationToCache,
} from '@/lib/realtime/notification-cache';
import { toast } from '@/components/ui/use-toast';
import { markReadByConversationId } from '@/lib/api/notifications';

/**
 *	App render component GlobalChatSocketListenerRefactor
 *	        ↓
 *	Lấy current user bằng useCurrentUser()
 *	        ↓
 *	Nếu có user thì ensureAuthSession()
 *	        ↓
 *	connectStomp()
 *	        ↓
 *	Tính activeConversationId
 *	        ↓
 *	syncSubscriptions()
 *	        ↓
 *	Khi socket có message thì gọi handleSocketMessage()
 *
 */

export function GlobalChatSocketListenerRefactor() {
  const pathname = usePathname();
  const queryClient = useQueryClient();
  const { data: me } = useCurrentUser();

  const stompConnected = useChatUiStore((s) => s.stompConnected);
  const selectedConversationId = useChatUiStore(
    (s) => s.selectedConversationId,
  );
  const adminConversationId = useChatUiStore((s) => s.adminConversationId);
  const adminWidgetOpen = useChatUiStore((s) => s.adminWidgetOpen);

  const aiConversationId = useChatUiStore((s) => s.aiConversationId);
  const aiWidgetOpen = useChatUiStore((s) => s.aiWidgetOpen);
  const conversationWidgetConversationId = useChatUiStore(
    (s) => s.conversationWidgetConversationId,
  );

  const conversationWidgetOpen = useChatUiStore(
    (s) => s.conversationWidgetOpen,
  );
  /**
   *	Nếu tin nhắn mới thuộc conversation đang mở
   *	→ user đã nhìn thấy
   *	→ không cần coi là unread
   *
   *	Nếu tin nhắn mới không thuộc conversation đang mở
   *	→ user chưa xem
   *	→ có thể tăng unread count
   */
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

  /**
   *	Có current user
   *	↓
   *	Gọi ensureAuthSession()
   *	↓
   *	Nếu session hợp lệ
   *	↓
   *	connectStomp()
   *	↓
   *	Kết nối websocket
   */
  useEffect(() => {
    if (!me) return;

    /* Để tránh trường hợp component unmount rồi nhưng async function vẫn chạy tiếp.
    Component mount
      → gọi API getMe
      → component unmount
      → API mới trả về
      → không nên connect socket nữa
    */
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

  /**
   *	Nếu chưa có userId
   *	→ unsubscribe socket sidebar
   *	→ unsubscribe socket conversation
   *
   *	Nếu có userId
   *	→ syncSubscriptions()
   *	→ subscribe sidebar của user
   *	→ subscribe conversation đang active nếu có
   *	Ví dụ:
   *	User A đang online
   *	→ nghe kênh sidebar của User A
   *
   *	User A mở conversation 123
   *	→ nghe thêm kênh conversation 123
   */

  useEffect(() => {
    if (!me?.userId) {
      unsubscribeSidebar();
      unsubscribeConversation();
      return;
    }

    // const onSocketMessage: MessageHandler = (frame: IMessage) => {
    //   handleSocketMessage({
    //     frame: frame,
    //     meUserId: me.userId,
    //     activeConversationId: activeConversationId,
    //     queryClient: queryClient,
    //   });
    // };
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
      //   onMessage: (frame) =>
      //     handleSocketMessage({
      //       frame,
      //       meUserId: me.userId,
      //       activeConversationId,
      //       queryClient,
      //     }),
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

/**	Flow
 *	Socket nhận frame
 *	↓
 *	JSON.parse(frame.body)
 *	↓
 *	Lấy envelope.type
 *	↓
 *	Switch theo type
 *	↓
 *	Gọi handler tương ứng
 *
 *	message.created
 *	→ có tin nhắn mới
 *
 *	conversation.updated
 *	→ thông tin conversation thay đổi
 *
 *	conversation.seen
 *	→ một user đã đọc tin nhắn trong conversation
 *
 *	notification.created
 *	→ có notification mới
 *
 *	notification.read
 *	→ một notification đã được đọc
 *
 *	notification.read-all
 *	→ tất cả notification đã được đọc
 */

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
/**
 *	Nhận message.created
 *	↓
 *	Lấy query key của message list theo conversationId
 *	↓
 *	Đưa message mới vào cache bằng upsertMessageInInfiniteCache()
 *	↓
 *	Invalidate lại danh sách message của conversation đó
 *	↓
 *	Invalidate lại danh sách conversations
 *	↓
 *	Kiểm tra message có thuộc active conversation không
 *	↓
 *	Nếu đang active thì return
 *	↓
 *	Nếu không active thì không làm gì thêm
 */

function handleMessageCreated(
  payload: MessageCreatedPayload,
  queryClient: QueryClient,
  activeConversationId: string | null,
) {
  const queryKey = queryKeys.infiniteMessages(payload.conversationId);

  // console.log('[ws] handleMessageCreated', {
  //   payload,
  //   activeConversationId,
  //   queryKey,
  // });

  /* tin nhắn mới hiện lên UI nhanh hơn, không cần đợi refetch. */
  queryClient.setQueryData<InfiniteData<Message[]> | undefined>(
    queryKey,
    (old) => upsertMessageInInfiniteCache(old, payload),
  );

  /* đồng bộ lại dữ liệu thật từ backend */
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
  // chat message notification thật sẽ do event notification.created xử lý
}

/**
 *	Nhận conversation.updated
 *	↓
 *	Invalidate query conversations
 *	↓
 *	Invalidate query messages của conversation đó
 *	Ví dụ
 *	- Last message thay đổi
 *	- Unread count của conversation thay đổi
 *	- Thứ tự conversation trong sidebar thay đổi
 *	- UpdatedAt thay đổi
 */

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

/**
 *	Nhận conversation.seen
 *	↓
 *	Kiểm tra người seen có phải mình không
 *	↓
 *	Nếu không phải mình
 *	→ cập nhật read state của người kia
 *	↓
 *	Invalidate conversations
 *	Đã xem
 *	Seen
 *	Read
 */

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

/**
 *	Nhận notification.created
 *	↓
 *	Thêm notification vào cache
 *	↓
 *	Tăng unread notification count
 *	↓
 *	Nếu nên hiện toast thì hiện toast
 *
 *	Đây là hàm liên quan trực tiếp đến bug
 *
 *	Vì hiện tại hàm này luôn tăng unread count.
 *
 *	Nó chưa kiểm tra:
 *
 *	Notification này có thuộc conversation đang mở không?
 *
 *	Nên dù user đang mở chat widget và đã thấy tin nhắn, chuông vẫn bị tăng.
 *	Nên hiểu hàm này như sau
 *
 *	Hiện tại:
 *	notification.created = luôn unread
 *
 *	Đúng hơn nên là:
 *	notification.created = unread nếu user chưa xem context đó
 */

function handleNotificationCreated(
  payload: NotificationCreatedPayload,
  queryClient: QueryClient,
  activeConversationId: string | null,
) {
  const notification = payload.notification;
  const conversationId = notification.target?.id;

  // console.log('[ws][notification.created]', {
  //   notificationType: notification.type,
  //   targetType: notification.target?.type,
  //   targetId: notification.target?.id,
  //   conversationId,
  //   activeConversationId,
  //   isSameConversation: conversationId === activeConversationId,
  // });
  const isActiveChatNotification =
    notification.type === 'chat.message.created' &&
    conversationId &&
    conversationId === activeConversationId;

  // console.log('[ws][notification.created] isActiveChatNotification', {
  //   isActiveChatNotification,
  // });

  if (isActiveChatNotification) {
    // console.log('[ws] active notification detected, mark read start', {
    //   conversationId,
    //   activeConversationId,
    // });
    markReadByConversationId(conversationId)
      .then(async (result) => {
        // console.log('[ws] markReadByConversationId success', result);
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
        console.log('[ws] unread count refetched');
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

/**
 *	Mục đích
 *
 *	Xử lý khi một notification được đánh dấu là đã đọc.
 *
 *	Flow
 *
 *	Nhận notification.read
 *	        ↓
 *	Cập nhật notification đó trong cache thành read
 *	        ↓
 *	Giảm unread notification count
 */

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

/**	
 *	Mục đích
 *	
 *	Xử lý khi tất cả notification được đánh dấu đã đọc.
 *	
 *	Flow
 *	
 *	Nhận notification.read-all
 *	        ↓
 *	Mark tất cả notification trong cache là read

 */
function handleNotificationReadAll(
  payload: NotificationReadAllPayload,
  queryClient: QueryClient,
) {
  markAllNotificationsReadInCache(queryClient, payload.readAt);
}

/**
 *	Mục đích
 *
 *	Quyết định notification nào thì hiện toast popup.
 *
 *	Hiện tại
 *
 *	Chỉ hiện toast nếu notification là tin nhắn chat mới:
 *
 *	notification.type === 'chat.message.created'
 *	Ý nghĩa
 *
 *	Nếu có notification loại khác, ví dụ:
 *
 *	system.alert
 *	order.updated
 *	comment.created
 *
 *	thì sẽ không hiện toast, theo logic hiện tại.
 *
 *	Liên quan bug
 *
 *	Hàm này cũng chưa biết user có đang mở conversation đó hay không.
 *
 *	Nên hiện tại có thể xảy ra:
 *
 *	User đang mở đúng chat
 *	→ tin nhắn mới hiện trong widget
 *	→ vẫn hiện toast báo tin nhắn mới
 *
 *	Nếu muốn UX tốt hơn, nên thêm điều kiện:
 *
 *	Nếu đang mở đúng conversation thì không toast
 *
 */

function shouldShowToast(
  notification: NotificationCreatedPayload['notification'],
) {
  return notification.type === 'chat.message.created';
}

/**
 *	Mục đích
 *
 *	Đảm bảo session đăng nhập hợp lệ trước khi connect websocket.
 *
 *	Flow
 *
 *	Gọi API getMe
 *	        ↓
 *	Nếu thành công
 *	→ user vẫn đăng nhập
 *	→ được connect socket
 *
 *	Nếu thất bại
 *	→ không connect socket
 *
 *	Vì sao dùng fetchQuery
 *
 *	Vì nó đi qua React Query cache.
 *
 *	Nó vừa gọi API, vừa cập nhật cache queryKeys.me.
 */
async function ensureAuthSession(queryClient: QueryClient) {
  await queryClient.fetchQuery({
    queryKey: queryKeys.me,
    queryFn: getMe,
    staleTime: 0,
  });
}

/**
 *	Mục đích
 *
 *	Thêm hoặc cập nhật tin nhắn mới vào cache dạng infinite query.
 *
 *	Dùng cho danh sách tin nhắn phân trang/infinite scroll.
 *
 *	Vì sao gọi là upsert
 *
 *	upsert = update + insert.
 *
 *	Nghĩa là:
 *
 *	Nếu message đã tồn tại
 *	→ cập nhật message đó
 *
 *	Nếu message chưa tồn tại
 *	→ thêm message mới vào cache
 */

function upsertMessageInInfiniteCache(
  old: InfiniteData<Message[]> | undefined,
  incoming: Message,
): InfiniteData<Message[]> {
  /* Trường hợp 1: Chưa có cache
    Nếu cache chưa có gì
    → tạo cache mới
    → page đầu tiên chứa incoming message
  */
  if (!old) {
    return {
      pages: [[incoming]],
      pageParams: [undefined],
    };
  }

  const exists = old.pages.some((page) =>
    page.some((message) => message.messageId === incoming.messageId),
  );

  /*Trường hợp 2: Message đã tồn tại trong cache
    Nếu message đã tồn tại trong cache
      Tìm đúng message cũ
    → merge dữ liệu mới vào
    → set isTemp = false để đánh dấu đây không còn là message tạm nữa
    Ví dụ:
    Frontend tạm hiển thị message đang gửi
    → isTemp = true
    Backend trả message thật qua socket
    → cập nhật lại message đó
    → isTemp = false
  */
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

  /* Trường hợp 3: Message chưa tồn tại
  Nếu chưa có page nào thì tạo page đầu tiên.
  */
  if (pages.length === 0) {
    return {
      ...old,
      pages: [[incoming]],
    };
  }

  // page đầu là page newest/current đang hiển thị
  /* Trường hợp 4: Có page rồi
  Thêm message mới vào page đầu tiên
  Tức là page đầu là phần tin nhắn mới nhất/current đang được render.
  */
  pages[0] = [...(pages[0] ?? []), { ...incoming, isTemp: false }];

  return {
    ...old,
    pages,
  };
}
