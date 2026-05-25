'use client';

import { useMemo } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { useMarkAllNotificationsRead } from '@/query/notifications/useMarkAllNotificationsRead';
import { useMarkNotificationRead } from '@/query/notifications/useMarkNotificationRead';
import { useNotifications } from '@/query/notifications/useNotifications';
import { useUnreadNotificationCount } from '@/query/notifications/useUnreadNotificationCount';
import type { AppNotification } from '@/utils/types';
import {
  chatUiActions,
  useChatUiReduxDispatch,
} from '@/stores/chat-ui-redux-store';
import { useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '@/query/query-keys';

type NotificationDropdownProps = {
  onItemClick?: () => void;
};
export function NotificationDropdown({
  onItemClick,
}: {
  onItemClick?: () => void;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const queryClient = useQueryClient();
  const dispatch = useChatUiReduxDispatch();
  // Zustand: useChatUiStore((s) => s.setSelectedConversationId)
  const setSelectedConversationId = (conversationId: string | null) =>
    dispatch(chatUiActions.setSelectedConversationId(conversationId));
  // Zustand: useChatUiStore((s) => s.setAiConversationId)
  const setAiConversationId = (conversationId: string | null) =>
    dispatch(chatUiActions.setAiConversationId(conversationId));
  // Zustand: useChatUiStore((s) => s.setAiWidgetOpen)
  const setAiWidgetOpen = (open: boolean) =>
    dispatch(chatUiActions.setAiWidgetOpen(open));
  // Zustand: useChatUiStore((s) => s.openConversationWidget)
  const openConversationWidget = (
    conversationId: string,
    type: 'USER_ADMIN' | 'DIRECT' | 'USER_DIRECT',
  ) => dispatch(chatUiActions.openConversationWidget({ conversationId, type }));

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage } =
    useNotifications();

  const { data: unreadCountData } = useUnreadNotificationCount();

  // const { mutate: markRead, isPending: isMarkingRead } =
  //   useMarkNotificationRead();

  const { mutate: markAllRead, isPending: isMarkingAllRead } =
    useMarkAllNotificationsRead();

  const notifications = useMemo(
    () => data?.pages.flatMap((page) => page.items) ?? [],
    [data],
  );

  const unreadCount = unreadCountData?.unreadCount ?? 0;

  const handleClickNotification = (notification: AppNotification) => {
    // if (!notification.read) {
    //   markRead(notification.notificationId);
    // }
    const targetUrl = notification.target?.url;
    const target = notification.target;
    const conversationId = target?.id ?? null;
    const targetType = target?.type ?? null;
    onItemClick?.();
    if (!target || !conversationId) {
      if (target?.url) {
        router.push(target.url);
      }
      return;
    }
    const isChatPage = pathname.startsWith('/chat');

    // 1) AI chat
    if (targetType === 'USER_AI') {
      if (isChatPage) {
        router.push(`/chat?conversationId=${conversationId}&type=ai`);
      } else {
        setAiConversationId(conversationId);
        setAiWidgetOpen(true);
      }
      return;
    }

    // 2) Admin / direct conversation
    if (
      targetType === 'USER_ADMIN' ||
      targetType === 'DIRECT' ||
      targetType === 'USER_DIRECT'
    ) {
      if (isChatPage) {
        const type = targetType === 'USER_ADMIN' ? 'admin' : 'direct';

        setSelectedConversationId(conversationId);
        queryClient.invalidateQueries({
          queryKey: queryKeys.conversations,
        });

        router.push(`/chat?conversationId=${conversationId}&type=${type}`);
      } else {
        openConversationWidget(
          conversationId,
          targetType === 'USER_ADMIN' ? 'USER_ADMIN' : 'USER_DIRECT',
        );
      }
      return;
    }

    if (target.url) {
      router.push(target.url);
    }
    // 3) Chat page thường (DIRECT / USER_ADMIN)
    // if (targetType === 'DIRECT' || targetType === 'USER_ADMIN') {
    //   setSelectedConversationId(conversationId);

    //   queryClient.invalidateQueries({
    //     queryKey: queryKeys.conversations,
    //   });

    //   router.push(`/chat?conversationId=${conversationId}`);
    //   return;
    // }
  };

  return (
    <div className='rounded-xl bg-white'>
      <div className='flex items-center justify-between border-b p-3'>
        <div className='font-semibold'>Thông báo</div>

        <div className='flex items-center gap-2'>
          <span className='text-sm text-gray-500'>{unreadCount} chưa đọc</span>
          <button
            type='button'
            onClick={() => markAllRead()}
            disabled={isMarkingAllRead}
            className='text-sm text-blue-600 disabled:opacity-50'
          >
            Đọc tất cả
          </button>
        </div>
      </div>

      <div className='max-h-[420px] overflow-y-auto'>
        {notifications.length === 0 ? (
          <div className='p-4 text-sm text-gray-500'>Chưa có thông báo</div>
        ) : (
          notifications.map((notification) => (
            <button
              key={notification.notificationId}
              type='button'
              onClick={() => handleClickNotification(notification)}
              className={`block w-full border-b px-4 py-3 text-left hover:bg-gray-50 ${
                notification.read ? 'bg-white' : 'bg-blue-50'
              }`}
            >
              <div className='mb-1 flex items-start justify-between gap-3'>
                <div className='font-medium'>{notification.title}</div>
                {!notification.read && (
                  <span className='mt-1 h-2 w-2 rounded-full bg-blue-500' />
                )}
              </div>

              <div className='text-sm text-gray-600'>{notification.body}</div>

              <div className='mt-2 text-xs text-gray-400'>
                {new Date(notification.createdAt).toLocaleString()}
              </div>
            </button>
          ))
        )}

        {hasNextPage && (
          <div className='p-3'>
            <button
              type='button'
              onClick={() => fetchNextPage()}
              disabled={isFetchingNextPage}
              className='w-full rounded-md border px-3 py-2 text-sm disabled:opacity-50'
            >
              {isFetchingNextPage ? 'Đang tải...' : 'Xem thêm'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
