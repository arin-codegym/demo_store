import { MessageCreatedPayload, User } from '@/utils/types';
import { useNotificationStore } from '@/stores/notification-store';
import { toast } from '@/components/ui/use-toast';

export function isAdmin(user: User | null) {
  return !!user?.roles?.includes('ROLE_ADMIN');
}

export function pushIncomingMessageNotice(payload: MessageCreatedPayload) {
  const notice = {
    id: `msg:${payload.messageId}`,
    type: 'new-message' as const,
    conversationId: payload.conversationId,
    senderName: payload.senderUserId,
    content: payload.content,
    createdAt: new Date().toISOString(),
    read: false,
  };

  const store = useNotificationStore.getState();
  store.addNotice(notice);

  toast({
    title: `${payload.conversationId || 'Người dùng'} gửi tin nhắn mới`,
    description: payload.content || 'Bạn có một tin nhắn mới',
  });
}
