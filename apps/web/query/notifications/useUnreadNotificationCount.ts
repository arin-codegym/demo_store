import { useQuery } from '@tanstack/react-query';
import { queryKeys } from '@/query/query-keys';
import { getUnreadNotificationCount } from '@/lib/api/notifications';

export function useUnreadNotificationCount(enabled = true) {
  return useQuery({
    queryKey: queryKeys.unreadNotificationCount(),
    queryFn: async () => getUnreadNotificationCount(),
    enabled,
  });
}
