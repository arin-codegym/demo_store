// import { fetchClient } from "./fetch-client";
import type {
  MarkSeenRequest,
  Message,
  SendMessageRequest,
} from '@/utils/types';
import {
  fetchJsonWithAuth,
  fetchWithAuth,
  postJsonWithAuth,
} from '../fetchWithAuth.client';

export async function getMessages(
  conversationId: string,
  params?: { beforeMessageId?: string; limit?: number },
) {
  const search = new URLSearchParams();

  if (params?.beforeMessageId) {
    search.set('beforeMessageId', params.beforeMessageId);
  }
  if (params?.limit) {
    search.set('limit', String(params.limit));
  }

  const queryString = search.toString();
  const path = `${process.env.NEXT_PUBLIC_BACKEND_URL}/conversations/${conversationId}/messages${
    queryString ? `?${queryString}` : ''
  }`;
  const data = await fetchJsonWithAuth<Message[]>(path, {
    method: 'GET',
  });
  return data ?? [];
}

export async function sendMessage(
  conversationId: string,
  payload: SendMessageRequest,
) {
  return postJsonWithAuth<Message>(
    `${process.env.NEXT_PUBLIC_BACKEND_URL}/conversations/${conversationId}/messages`,
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
  );
}

export async function markSeen(
  conversationId: string,
  payload: MarkSeenRequest,
) {
  return postJsonWithAuth<void>(
    `${process.env.NEXT_PUBLIC_BACKEND_URL}/conversations/${conversationId}/seen`,
    payload,
  );
}
