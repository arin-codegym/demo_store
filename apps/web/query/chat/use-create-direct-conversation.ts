'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createDirectConversation } from '@/lib/api/chat/create-direct-conversation';
import { queryKeys } from '@/query/query-keys';

export function useCreateDirectConversation() {
  const queryClient = useQueryClient();

  return useMutation({
    /* Viết tường minh */
    // mutationFn: async (variables: CreateDirectConversationRequest) => {
    //   return createDirectConversation(variables);
    // },
    mutationFn: createDirectConversation,
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: queryKeys.conversations,
      });
    },
  });
}
