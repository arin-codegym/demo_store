import { fetchWithAuth } from '../fetchWithAuth.client';
import type {
  ConversationSummary,
  CreateDirectConversationRequest,
} from '@/utils/types';

export async function getConversations() {
  const response = await fetchWithAuth<ConversationSummary[]>(
    `${process.env.NEXT_PUBLIC_BACKEND_URL}/conversations`,
    {
      method: 'GET',
    },
  );
  if (!response.ok) {
    throw new Error('Network response was not ok');
  }

  // BẠN ĐANG THIẾU BƯỚC NÀY: Chuyển Response thành JSON
  const data = await response.json();
  return data;
}

export async function createOrGetDirectConversation(
  payload: CreateDirectConversationRequest,
) {
  return fetchWithAuth<{ conversationId: string }>(
    `${process.env.NEXT_PUBLIC_BACKEND_URL}/conversations/direct`,
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
  );
}
