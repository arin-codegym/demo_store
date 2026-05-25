import type { InfiniteData, QueryClient } from '@tanstack/react-query';
import { queryKeys } from '@/query/query-keys';
import type { AppNotification, NotificationListResponse } from '@/utils/types';

type NotificationInfiniteData = InfiniteData<NotificationListResponse>;

// These helpers only update React Query cache. Server persistence happens in
// the API calls that trigger the corresponding realtime event.
export function prependNotificationToCache(
  queryClient: QueryClient,
  notification: AppNotification,
) {
  queryClient.setQueriesData<NotificationInfiniteData>(
    { queryKey: ['notifications', 'infinite'] },
    (old) => {
      if (!old) return old;

      const alreadyExists = old.pages.some((page) =>
        page.items.some(
          (item) => item.notificationId === notification.notificationId,
        ),
      );

      if (alreadyExists) return old;

      if (old.pages.length === 0) {
        return {
          pageParams: [null],
          pages: [
            {
              items: [notification],
              nextCursor: null,
            },
          ],
        };
      }

      return {
        ...old,
        pages: [
          {
            ...old.pages[0],
            items: [notification, ...old.pages[0].items],
          },
          ...old.pages.slice(1),
        ],
      };
    },
  );
}

export function increaseUnreadNotificationCount(queryClient: QueryClient) {
  queryClient.setQueryData(
    queryKeys.unreadNotificationCount(),
    (old: { unreadCount: number } | undefined) => ({
      unreadCount: (old?.unreadCount ?? 0) + 1,
    }),
  );
}

export function markNotificationReadInCache(
  queryClient: QueryClient,
  notificationId: string,
  readAt: string,
) {
  queryClient.setQueriesData<NotificationInfiniteData>(
    { queryKey: ['notifications', 'infinite'] },
    (old) => {
      if (!old) return old;

      let changed = false;

      const pages = old.pages.map((page) => ({
        ...page,
        items: page.items.map((item) => {
          if (item.notificationId !== notificationId) return item;
          if (item.read) return item;

          changed = true;
          return {
            ...item,
            read: true,
            readAt,
          };
        }),
      }));

      return changed ? { ...old, pages } : old;
    },
  );
}

export function markAllNotificationsReadInCache(
  queryClient: QueryClient,
  readAt: string,
) {
  queryClient.setQueriesData<NotificationInfiniteData>(
    { queryKey: ['notifications', 'infinite'] },
    (old) => {
      if (!old) return old;

      return {
        ...old,
        pages: old.pages.map((page) => ({
          ...page,
          items: page.items.map((item) =>
            item.read
              ? item
              : {
                  ...item,
                  read: true,
                  readAt,
                },
          ),
        })),
      };
    },
  );

  queryClient.setQueryData(queryKeys.unreadNotificationCount(), {
    unreadCount: 0,
  });
}

export function decreaseUnreadNotificationCount(queryClient: QueryClient) {
  queryClient.setQueryData(
    queryKeys.unreadNotificationCount(),
    (old: { unreadCount: number } | undefined) => ({
      unreadCount: Math.max((old?.unreadCount ?? 1) - 1, 0),
    }),
  );
}
