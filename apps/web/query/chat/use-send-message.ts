'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { sendMessage } from '@/lib/api/chat/send-message';
import { queryKeys } from '@/query/query-keys';
import type { Message, SendMessageRequest } from '@/utils/types';
import { fetchWithAuth } from '@/lib/fetchWithAuth.client';
import { SendMessageResponse } from '@/utils/types';

export function useSendMessage() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: SendMessageRequest) => sendMessage(body),

    onMutate: async (newMessage) => {
      const queryKey = queryKeys.messages(newMessage.conversationId);

      await queryClient.cancelQueries({ queryKey });

      const previousMessages =
        queryClient.getQueryData<Message[]>(queryKey) ?? [];

      const optimisticMessage: Message = {
        messageId: newMessage.clientMessageId,
        conversationId: newMessage.conversationId,
        senderUserId: 'me',
        senderType: 'USER',
        content: newMessage.content,
        clientMessageId: newMessage.clientMessageId,
        createdAt: new Date().toISOString(),
      };

      queryClient.setQueryData<Message[]>(queryKey, [
        ...previousMessages,
        optimisticMessage,
      ]);

      return { previousMessages, queryKey };
    },

    onError: (_error, _variables, context) => {
      if (!context) return;
      queryClient.setQueryData(context.queryKey, context.previousMessages);
    },

    onSuccess: (savedMessage) => {
      if (savedMessage == null) {
        return;
      }
      queryClient.setQueryData<Message[]>(
        queryKeys.messages(savedMessage.conversationId),
        (old = []) => {
          const filtered = old.filter(
            (m) => m.clientMessageId !== savedMessage.clientMessageId,
          );
          return [...filtered, savedMessage];
        },
      );
    },

    onSettled: (_data, _error, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.messages(variables.conversationId),
      });
      queryClient.invalidateQueries({
        queryKey: queryKeys.conversations,
      });
    },
  });
}
