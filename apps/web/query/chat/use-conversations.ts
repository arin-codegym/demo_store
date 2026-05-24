'use client';

import { useQuery } from '@tanstack/react-query';
import { getConversations } from '@/lib/api/chat/get-conversations';
import { queryKeys } from '@/query/query-keys';
import { ConversationSummary } from '@/utils/types';

export function useConversations() {
  return useQuery<ConversationSummary[]>({
    queryKey: queryKeys.conversations,
    queryFn: getConversations,
  });
}
