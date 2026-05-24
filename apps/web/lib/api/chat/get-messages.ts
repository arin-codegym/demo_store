import type { Message } from '@/utils/types';
import { fetchJsonWithAuth } from '@/lib/fetchWithAuth.client';

export function getMessages(conversationId: string) {
  return fetchJsonWithAuth<Message[]>(
    `/conversations/${conversationId}/messages`,
    {
      method: 'GET',
    },
  );
}
