import { getMessages } from '@/lib/api/messages.client';
import { useInfiniteQuery } from '@tanstack/react-query';
import { queryKeys } from '../query-keys';

type Message = {
  messageId: string;
  content: string;
  senderUserId: string | null;
  senderType: 'USER' | 'AI';
  createdAt: string;
};

export function useInfiniteMessages(conversationId: string | null) {
  return useInfiniteQuery<Message[]>({
    // queryKey: ['messages', conversationId], kiểu củ dùng của query messages
    queryKey: conversationId
      ? queryKeys.infiniteMessages(conversationId)
      : ['messages', 'null', 'infinite'],

    queryFn: async ({ pageParam }) => {
      if (!conversationId) return [];
      // console.log('[query] fetch messages', {
      //   conversationId,
      //   pageParam,
      // });
      const result = await getMessages(conversationId, {
        limit: 30,
        beforeMessageId: typeof pageParam === 'string' ? pageParam : undefined,
      });
      // console.log('[query] fetch messages result', {
      //   conversationId,
      //   pageParam,
      //   count: Array.isArray(result) ? result.length : -1,
      //   messages: Array.isArray(result)
      //     ? result.map((m) => ({
      //         messageId: m.messageId,
      //         senderType: m.senderType,
      //         content: m.content,
      //         createdAt: m.createdAt,
      //       }))
      //     : result,
      // });
      return Array.isArray(result) ? result : [];
    },

    enabled: !!conversationId,
    initialPageParam: undefined,
    getNextPageParam: (lastPage) => {
      if (!Array.isArray(lastPage) || lastPage.length < 30) {
        return undefined;
      }

      return lastPage[lastPage.length - 1]?.messageId;
    },
  });
}

// export function useInfiniteMessages(conversationId: string | null) {
//   return useInfiniteQuery({
//     queryKey: ['messages', conversationId],
//     queryFn: async ({ pageParam }: { pageParam?: string | null }) => {
//       return getMessages(conversationId!, {
//         limit: 30,
//         beforeMessageId: pageParam ?? undefined,
//       });
//     },
//     enabled: !!conversationId,
//     initialPageParam: null,
//     getNextPageParam: (lastPage: Message[]) => {
//       if (!lastPage || lastPage.length < 30) return undefined;

//       const oldestMessage = lastPage[lastPage.length - 1];
//       return oldestMessage?.messageId ?? undefined;
//     },
//   });
// }
