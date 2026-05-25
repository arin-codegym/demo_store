'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import { useChatUiReduxSelector } from '@/stores/chat-ui-redux-store';
import { useConversations } from '@/query/chat/use-conversations';
import { MessageList } from './message-list';
import { MessageComposer } from '@/components/chat/message-composer';
import { ChatHeader } from './chat-header';
import { useCurrentUser } from '@/query/auth/useCurrentUser';
import { markSeen } from '@/lib/api/messages.client';
import { useConversationReadState } from '@/query/chat/use-conversation-read-state';
import { useInfiniteMessages } from '../../query/chat/use-infinite-messages';

export function MessagePanel() {
  const { data: me } = useCurrentUser();
  // Zustand: useChatUiStore((s) => s.selectedConversationId)
  const selectedConversationId = useChatUiReduxSelector(
    (state) => state.chatUi.selectedConversationId,
  );

  const { data: conversations = [] } = useConversations();
  const { data: readState } = useConversationReadState(selectedConversationId);

  const { data, isLoading, fetchNextPage, hasNextPage, isFetchingNextPage } =
    useInfiniteMessages(selectedConversationId);

  const [isNearBottom, setIsNearBottom] = useState(true);
  const lastMarkedMessageIdRef = useRef<string | null>(null);

  const selectedConversation = useMemo(
    () =>
      conversations?.find(
        (item) => item.conversationId === selectedConversationId,
      ) ?? null,
    [conversations, selectedConversationId],
  );

  const messages = useMemo(() => {
    const raw = data?.pages.flatMap((page) => page ?? []) ?? [];
    const uniqueMessages = Array.from(
      new Map(raw.map((message) => [message.messageId, message])).values(),
    );

    return uniqueMessages.sort((a, b) => {
      const timeDiff =
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();

      if (timeDiff !== 0) return timeDiff;
      return a.messageId.localeCompare(b.messageId);
    });
  }, [data]);

  function isUuid(value?: string | null) {
    if (!value) return false;
    return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(
      value,
    );
  }

  useEffect(() => {
    lastMarkedMessageIdRef.current = null;
    setIsNearBottom(true);
  }, [selectedConversationId]);

  useEffect(() => {
    if (!selectedConversationId || !me?.userId) return;
    if (!isNearBottom) return;
    if (messages.length === 0) return;

    const lastMessage = messages[messages.length - 1];
    if (!lastMessage) return;

    if (lastMessage.senderUserId === me.userId) return;
    if (!isUuid(lastMessage.messageId)) return;
    if (lastMarkedMessageIdRef.current === lastMessage.messageId) return;

    lastMarkedMessageIdRef.current = lastMessage.messageId;

    void markSeen(selectedConversationId, {
      lastReadMessageId: lastMessage.messageId,
    }).catch((error) => {
      console.error('markSeen failed', error);
      lastMarkedMessageIdRef.current = null;
    });
  }, [selectedConversationId, messages, me?.userId, isNearBottom]);

  if (!selectedConversationId) {
    return (
      <div className='flex flex-1 items-center justify-center bg-slate-50'>
        <div className='rounded-2xl border border-slate-200 bg-white px-8 py-10 text-center shadow-sm'>
          <div className='text-lg font-semibold text-slate-900'>
            Chưa chọn cuộc trò chuyện
          </div>
          <div className='mt-2 text-sm text-slate-500'>
            Hãy chọn một conversation ở sidebar để bắt đầu chat
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className='flex min-h-0 min-w-0 flex-1 flex-col bg-slate-50'>
      <ChatHeader />

      <div className='min-h-0 flex-1'>
        <MessageList
          conversationId={selectedConversationId}
          messages={messages}
          isLoading={isLoading}
          onNearBottomChange={setIsNearBottom}
          otherUserLastReadMessageId={readState?.otherUserLastReadMessageId}
          hasMore={Boolean(hasNextPage)}
          isFetchingMore={isFetchingNextPage}
          onLoadMore={() => {
            if (!selectedConversationId) return;
            if (!hasNextPage || isFetchingNextPage) return;
            void fetchNextPage();
          }}
        />
      </div>

      <div className='border-t border-slate-200 bg-white px-4 py-3'>
        <MessageComposer conversationId={selectedConversationId} />
      </div>
    </div>
  );
}
