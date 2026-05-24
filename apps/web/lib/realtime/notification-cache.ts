import type { InfiniteData, QueryClient } from '@tanstack/react-query';
import { queryKeys } from '@/query/query-keys';
import type { AppNotification, NotificationListResponse } from '@/utils/types';

type NotificationInfiniteData = InfiniteData<NotificationListResponse>;

/**
 *	Hàm này thêm notification mới vào đầu danh sách notification đang cache ở frontend.
 *
 *	Mục đích:
 *	Khi user mở chuông thông báo, notification mới xuất hiện ngay
 *	mà không cần reload trang.
 *
 *	Các hàm khác trong file này sẽ cập nhật cache khi có sự kiện liên quan đến notification xảy ra,
 *	như đánh dấu đã đọc, xóa notification,... để đảm bảo cache luôn đồng bộ với trạng thái thực tế của notification trên server.
 *
 * Note: Các hàm này chỉ cập nhật cache ở frontend, không gửi bất kỳ request nào lên server.
 *  Việc đồng bộ với server sẽ được xử lý thông qua các API khác khi user thực hiện hành động liên quan đến notification.
 *
 */

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

/**
 *	Hàm này tăng số notification chưa đọc trên chuông.
 *
 *	Ví dụ:
 *	Đang là 2 notification chưa đọc
 *	Có notification mới
 *	→ tăng thành 3 notification chưa đọc
 *
 *	Mục đích: khi có notification mới mà user chưa mở chuông ra xem, số lượng notification chưa đọc trên chuông sẽ tăng lên để thu hút sự chú ý của user.
 *
 *	Các hàm khác trong file này sẽ cập nhật cache khi có sự kiện liên quan đến notification xảy ra, như đánh dấu đã đọc, xóa notification,... để đảm bảo cache luôn đồng bộ với trạng thái thực tế của notification trên server.
 *
 * Note: Các hàm này chỉ cập nhật cache ở frontend, không gửi bất kỳ request nào lên server.
 *  Việc đồng bộ với server sẽ được xử lý thông qua các API khác khi user thực hiện hành động liên quan đến notification.
 */

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
