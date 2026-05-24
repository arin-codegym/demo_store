'use client';

import type { Message } from '@/utils/types';
import { MessageBubble } from './message-bubble';
import { useCurrentUser } from '@/query/auth/useCurrentUser';

type Props = {
  messages: Message[];
  isLoading: boolean;
};

export function MessageList({ messages, isLoading }: Props) {
  const { data: me } = useCurrentUser();

  if (isLoading) {
    return <div className='p-4 text-sm'>Loading messages...</div>;
  }

  if (messages.length === 0) {
    return (
      <div className='flex h-full items-center justify-center text-sm text-gray-500'>
        Chưa có tin nhắn nào
      </div>
    );
  }

  return (
    <div className='flex h-full flex-col gap-3 overflow-y-auto px-4 py-4'>
      {messages.map((message) => (
        <MessageBubble
          key={message.messageId}
          content={message.content}
          createdAtLabel={formatMessageTime(message.createdAt)}
          isMine={message.senderUserId === me?.userId}
          isAi={message.senderType === 'AI'}
        />
      ))}
    </div>
  );
}

function formatMessageTime(value: string) {
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}
