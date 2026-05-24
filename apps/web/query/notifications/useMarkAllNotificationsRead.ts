import { useMutation, useQueryClient } from '@tanstack/react-query';
import { markAllNotificationsRead } from '@/lib/api/notifications';
import { queryKeys } from '@/query/query-keys';

export function useMarkAllNotificationsRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => markAllNotificationsRead(),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['notifications'],
      });
      queryClient.invalidateQueries({
        queryKey: queryKeys.unreadNotificationCount(),
        exact: true,
      });
    },
  });
}
