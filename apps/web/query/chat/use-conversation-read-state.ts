import { useQuery } from '@tanstack/react-query';
import { queryKeys } from '@/query/query-keys';

export type ConversationReadState = {
  otherUserLastReadMessageId?: string | null;
  seenUserId?: string | null;
};

export function useConversationReadState(conversationId: string | null) {
  return useQuery<ConversationReadState>({
    queryKey: conversationId
      ? queryKeys.conversationReadState(conversationId)
      : ['conversation-read-state', 'empty'],
    queryFn: async () => ({
      otherUserLastReadMessageId: null,
      seenUserId: null,
    }),
    enabled: !!conversationId,
    staleTime: Infinity,
    gcTime: Infinity,
  });
}
