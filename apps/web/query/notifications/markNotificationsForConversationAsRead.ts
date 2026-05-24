import { AppNotification } from '@/utils/types';
import type { QueryClient } from '@tanstack/react-query';
import { queryKeys } from '../query-keys';

type MarkReadFn = (notificationId: string) => void;

export function markNotificationsForConversationAsRead(
  conversationId: string,
  notifications: AppNotification[],
  markRead: MarkReadFn,
  queryClient?: QueryClient,
) {
  const matched = notifications.filter(
    (n) => !n.read && n.target?.id === conversationId,
  );

  if (matched.length === 0) return;

  matched.forEach((n) => {
    markRead(n.notificationId);
  });

  if (queryClient) {
    queryClient.invalidateQueries({
      queryKey: queryKeys.infiniteNotifications(), // match theo prefix ví dụ key ['notifications', 'infinite', underfine] ['notifications', 'infinite', true]....
      //   exact: true, // nếu muốn match duy nhất key thì dùng cái này
    });
    queryClient.invalidateQueries({
      queryKey: ['notifications', 'unread-count'],
    });
  }
}
