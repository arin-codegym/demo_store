import type { ConversationSummary } from '@/utils/types';
import { fetchJsonWithAuth } from '@/lib/fetchWithAuth.client';

export async function getConversations() {
  const res = await fetchJsonWithAuth<ConversationSummary[] | null>(
    `${process.env.NEXT_PUBLIC_BACKEND_URL}/conversations`,
    {
      method: 'GET',
    },
  );

  return res ?? [];
}
