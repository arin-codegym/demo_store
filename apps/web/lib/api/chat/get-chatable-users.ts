import { fetchJsonWithAuth } from '@/lib/fetchWithAuth.client';
import type { ChatableUser } from '@/utils/types';

export function getChatableUsers(keyword: string) {
  const params = new URLSearchParams();
  if (keyword.trim()) {
    params.set('keyword', keyword.trim());
  }

  const query = params.toString();
  return fetchJsonWithAuth<ChatableUser[]>(
    `/api/chat/user/chatable${query ? `?${query}` : ''}`,
    {
      method: 'GET',
    },
  );
}
