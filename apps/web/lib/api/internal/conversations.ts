import { fetchJsonWithAuth } from '@/lib/fetchWithAuth.client';

export async function ensureAdminConversation(): Promise<string | null> {
  const data = await fetchJsonWithAuth<{ conversationId: string }>(
    '/api/user/conversations/admin/me',
    {
      method: 'GET',
    },
  );

  return data?.conversationId ?? null;
}

export async function ensureAiConversation(
  assistantCode = 'general',
): Promise<string | null> {
  const data = await fetchJsonWithAuth<{ conversationId: string }>(
    `/api/user/conversations/ai/me?assistantCode=${encodeURIComponent(assistantCode)}`,
    {
      method: 'GET',
    },
  );

  return data?.conversationId ?? null;
}
