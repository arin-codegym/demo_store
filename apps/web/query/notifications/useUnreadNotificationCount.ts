import { useQuery } from '@tanstack/react-query';
import { queryKeys } from '@/query/query-keys';
import { getUnreadNotificationCount } from '@/lib/api/notifications';

export function useUnreadNotificationCount() {
  return useQuery({
    queryKey: queryKeys.unreadNotificationCount(),
    queryFn: async () => {
      const data = await getUnreadNotificationCount();
      return data;
    },
  });
}
