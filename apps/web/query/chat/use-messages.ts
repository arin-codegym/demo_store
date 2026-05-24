'use client';

import { useQuery } from '@tanstack/react-query';
import { getMessages } from '@/lib/api/messages.client';

export function useMessages(conversationId: string | null) {
  return useQuery({
    queryKey: ['messages', conversationId],
    queryFn: () => getMessages(conversationId!, { limit: 30 }),
    enabled: !!conversationId,
  });
}
