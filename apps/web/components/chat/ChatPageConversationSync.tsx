'use client';

import { useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import { useQueryClient } from '@tanstack/react-query';

import {
  chatUiActions,
  useChatUiReduxDispatch,
} from '@/stores/chat-ui-redux-store';
import { queryKeys } from '@/query/query-keys';

export function ChatPageConversationSync() {
  const searchParams = useSearchParams();
  const queryClient = useQueryClient();

  const dispatch = useChatUiReduxDispatch();

  const conversationId = searchParams.get('conversationId');

  useEffect(() => {
    if (!conversationId) return;

    // Zustand: setSelectedConversationId(conversationId)
    dispatch(chatUiActions.setSelectedConversationId(conversationId));

    queryClient.invalidateQueries({
      queryKey: queryKeys.conversations,
    });
  }, [conversationId, dispatch, queryClient]);

  return null;
}
