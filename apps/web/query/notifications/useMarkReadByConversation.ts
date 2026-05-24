import { useMutation, useQueryClient } from '@tanstack/react-query';
import { markReadByConversationId } from '@/lib/api/notifications';
import { queryKeys } from '../query-keys';

export function useMarkReadByConversationId() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (conversationId: string) => {
      const res = await markReadByConversationId(conversationId);
      return res;
    },
    onSuccess: async (_, conversationId) => {
      await queryClient.invalidateQueries({
        queryKey: queryKeys.unreadNotificationCount(),
        exact: true,
      });

      await queryClient.invalidateQueries({
        queryKey: queryKeys.infiniteNotifications(),
      });

      await queryClient.refetchQueries({
        queryKey: queryKeys.unreadNotificationCount(),
        exact: true,
        type: 'active',
      });

      await queryClient.refetchQueries({
        queryKey: queryKeys.infiniteNotifications(),
        type: 'active',
      });
    },
    onError: (error, conversationId) => {
      console.error('[markRead] onError', conversationId, error);
    },
  });
}
