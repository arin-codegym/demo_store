'use client';

import { useQuery } from '@tanstack/react-query';
import { getChatableUsers } from '@/lib/api/chat/get-chatable-users';
import { queryKeys } from '@/query/query-keys';

export function useChatableUsers(keyword: string, enabled = true) {
  return useQuery({
    queryKey: queryKeys.chatableUsers(keyword),
    queryFn: () => getChatableUsers(keyword),
    enabled,
    staleTime: 30_000,
  });
}
