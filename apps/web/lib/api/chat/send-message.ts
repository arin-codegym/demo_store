import type { SendMessageRequest, SendMessageResponse } from '@/utils/types';
import { fetchJsonWithAuth } from '@/lib/fetchWithAuth.client';

export function sendMessage(body: SendMessageRequest) {
  return fetchJsonWithAuth<SendMessageResponse>('/api/chat/messages', {
    method: 'POST',
    body: JSON.stringify(body),
  });
}
