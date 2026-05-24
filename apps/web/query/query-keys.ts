// src/lib/query/query-keys.ts
export const queryKeys = {
  me: ['me'] as const,
  conversations: ['conversations'] as const,
  messages: (conversationId: string) => ['messages', conversationId] as const,
  chatableUsers: (keyword: string) => ['chatable-users', keyword] as const,
  conversationReadState: (conversationId: string) =>
    ['conversation-read-state', conversationId] as const,
  infiniteMessages: (conversationId: string) => [
    'messages',
    conversationId,
    'infinite',
  ],
  notifications: (params?: { unreadOnly?: boolean }) =>
    ['notifications', params] as const,

  infiniteNotifications: (params?: { unreadOnly?: boolean }) =>
    ['notifications', 'infinite', params] as const,

  unreadNotificationCount: () => ['notifications', 'unread-count'] as const,
};
