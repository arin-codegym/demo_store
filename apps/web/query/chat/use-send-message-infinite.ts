import { sendMessage } from '@/lib/api/chat/send-message';
import { Message, SendMessageRequest } from '@/utils/types';
import {
  InfiniteData,
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';
import { queryKeys } from '../query-keys';

function isSameMessage(left: Message, right: Message) {
  return (
    left.messageId === right.messageId ||
    (!!left.clientMessageId && left.clientMessageId === right.clientMessageId)
  );
}

function upsertInFirstPage(
  old: InfiniteData<Message[]> | undefined,
  message: Message,
): InfiniteData<Message[]> {
  if (!old) {
    return {
      pages: [[message]],
      pageParams: [undefined],
    };
  }

  let replaced = false;
  const pages = old.pages.map((page) => {
    const nextPage: Message[] = [];

    for (const existingMessage of page) {
      if (!isSameMessage(existingMessage, message)) {
        nextPage.push(existingMessage);
        continue;
      }

      if (!replaced) {
        replaced = true;
        nextPage.push({
          ...existingMessage,
          ...message,
          isTemp: false,
        });
      }
    }

    return nextPage;
  });

  if (replaced) {
    return {
      ...old,
      pages,
    };
  }

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
        (old) => upsertInFirstPage(old, optimisticMessage),
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

          return upsertInFirstPage(old, savedMessage);
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
