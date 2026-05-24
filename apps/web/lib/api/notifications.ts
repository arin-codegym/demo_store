import type {
  NotificationListResponse,
  UnreadNotificationCountResponse,
} from '@/utils/types';
import { fetchWithAuth } from '../fetchWithAuth.client';

type GetNotificationsParams = {
  cursor?: string | null;
  limit?: number;
  unreadOnly?: boolean;
};

function buildQuery(params: GetNotificationsParams) {
  const searchParams = new URLSearchParams();

  if (params.cursor) searchParams.set('cursor', params.cursor);
  if (params.limit) searchParams.set('limit', String(params.limit));
  if (params.unreadOnly) searchParams.set('unreadOnly', 'true');

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export async function getNotifications(
  params: GetNotificationsParams = {},
): Promise<NotificationListResponse> {
  const res = await fetchWithAuth(`/api/notifications${buildQuery(params)}`, {
    method: 'GET',
    credentials: 'include',
  });

  if (!res.ok) {
    throw new Error('Failed to fetch notifications');
  }

  return res.json();
}

export async function getUnreadNotificationCount(): Promise<UnreadNotificationCountResponse> {
  const res = await fetchWithAuth('/api/notifications/unread-count', {
    method: 'GET',
    credentials: 'include',
    cache: 'no-cache',
  });

  if (!res.ok) {
    throw new Error('Failed to fetch unread notification count');
  }

  return res.json();
}

export async function markNotificationRead(notificationId: string) {
  console.log('notification item', notificationId);
  const res = await fetchWithAuth(`/api/notifications/${notificationId}/read`, {
    method: 'POST',
    credentials: 'include',
  });

  if (!res.ok) {
    throw new Error('Failed to mark notification as read');
  }

  return res.json();
}

export async function markAllNotificationsRead() {
  const res = await fetchWithAuth('/api/notifications/read-all', {
    method: 'POST',
    credentials: 'include',
  });

  if (!res.ok) {
    throw new Error('Failed to mark all notifications as read');
  }

  return res.json();
}

export async function markReadByConversationId(conversationId: string) {
  const res = await fetchWithAuth('/api/notifications/read-by-conversation', {
    method: 'POST',
    body: JSON.stringify({ conversationId }),
  });
  const raw = await res.text();

  if (!res.ok) {
    throw new Error(`Failed to mark read: ${res.status} - ${raw}`);
  }

  return raw ? JSON.parse(raw) : null;
}
