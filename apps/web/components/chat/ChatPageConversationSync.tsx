'use client';

import { useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import { useQueryClient } from '@tanstack/react-query';

import { useChatUiStore } from '@/stores/chat-ui-store';
import { queryKeys } from '@/query/query-keys';

export function ChatPageConversationSync() {
  const searchParams = useSearchParams();
  const queryClient = useQueryClient();

  const setSelectedConversationId = useChatUiStore(
    (s) => s.setSelectedConversationId,
  );

  const conversationId = searchParams.get('conversationId');

  useEffect(() => {
    if (!conversationId) return;

    setSelectedConversationId(conversationId);

    queryClient.invalidateQueries({
      queryKey: queryKeys.conversations,
    });
  }, [conversationId, setSelectedConversationId, queryClient]);

  return null;
}
