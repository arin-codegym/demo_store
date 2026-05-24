import type {
  CreateDirectConversationRequest,
  CreateDirectConversationResponse,
} from '@/utils/types';
import { fetchJsonWithAuth } from '@/lib/fetchWithAuth.client';

export function createDirectConversation(
  body: CreateDirectConversationRequest,
) {
  return fetchJsonWithAuth<CreateDirectConversationResponse>(
    '/api/chat/conversations/direct',
    {
      method: 'POST',
      body: JSON.stringify(body),
    },
  );
}
