import { sendMessage } from '@/lib/api/chat/send-message';
import { Message, SendMessageRequest } from '@/utils/types';
import {
  InfiniteData,
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';
import { queryKeys } from '../query-keys';

function appendToFirstPage(
  old: InfiniteData<Message[]> | undefined,
  message: Message,
): InfiniteData<Message[]> {
  if (!old) {
    return {
      pages: [[message]],
      pageParams: [undefined],
    };
  }

  const pages = [...old.pages];

  if (pages.length === 0) {
    return {
      ...old,
      pages: [[message]],
    };
  }

  const firstPage = pages[0] ?? [];
  pages[0] = [...firstPage, message];

  return {
    ...old,
    pages,
  };
}

export function useSendMessageInfinite(currentUserId?: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: SendMessageRequest) => sendMessage(body),

    onMutate: async (newMessage) => {
      const queryKey = queryKeys.infiniteMessages(newMessage.conversationId);

      await queryClient.cancelQueries({ queryKey });

      const previousData =
        queryClient.getQueryData<InfiniteData<Message[]>>(queryKey);

      const optimisticMessage: Message = {
        messageId: newMessage.clientMessageId,
        conversationId: newMessage.conversationId,
        senderUserId: currentUserId ?? '',
        senderType: 'USER',
        clientMessageId: newMessage.clientMessageId,
        content: newMessage.content,
        status: 'PENDING',
        createdAt: new Date().toISOString(),
        isTemp: true,
      };

      queryClient.setQueryData<InfiniteData<Message[]> | undefined>(
        queryKey,
        (old) => appendToFirstPage(old, optimisticMessage),
      );

      return { previousData, queryKey };
    },

    onError: (_error, _variables, context) => {
      if (!context) return;
      queryClient.setQueryData(context.queryKey, context.previousData);
    },

    onSuccess: (savedMessage) => {
      if (!savedMessage) return;

      queryClient.setQueryData<InfiniteData<Message[]> | undefined>(
        queryKeys.infiniteMessages(savedMessage.conversationId),
        (old) => {
          if (!old) {
            return {
              pages: [[savedMessage]],
              pageParams: [undefined],
            };
          }

          let replaced = false;

          const pages = old.pages.map((page) =>
            page.map((message) => {
              if (message.clientMessageId === savedMessage.clientMessageId) {
                replaced = true;
                return {
                  ...savedMessage,
                  isTemp: false,
                };
              }
              return message;
            }),
          );

          if (!replaced) {
            return appendToFirstPage(
              {
                ...old,
                pages,
              },
              savedMessage,
            );
          }

          return {
            ...old,
            pages,
          };
        },
      );
    },

    onSettled: (_data, _error, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.infiniteMessages(variables.conversationId),
        refetchType: 'active',
      });

      queryClient.invalidateQueries({
        queryKey: queryKeys.conversations,
      });
    },
  });
}
