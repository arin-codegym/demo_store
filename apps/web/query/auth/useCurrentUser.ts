// 'use client';

import { useQuery } from '@tanstack/react-query';
import { User } from '@/utils/types';
import { fetchJsonWithAuth, fetchWithAuth } from '@/lib/fetchWithAuth.client';
import { queryKeys } from '../query-keys';
import { getMe } from '@/lib/api/get-me';

export function useCurrentUser() {
  return useQuery<User | null>({
    queryKey: queryKeys.me,
    queryFn: async () => {
      // return fetchJsonWithAuth<User | null>('/api/auth/me');
      const data = await getMe();
      return data;
    },
    retry: false,
    staleTime: 5 * 60_000, // 60s => devtools sẽ hiện Fresh trong 5 phút
  });
}
