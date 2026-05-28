'use client';

import { useEffect, useMemo, useRef } from 'react';
import { MessageBubble } from './message-bubble';
import { useCurrentUser } from '@/query/auth/useCurrentUser';

type Message = {
  messageId: string;
  content: string;
  senderUserId: string | null;
  senderType: 'USER' | 'AI';
  createdAt: string;
};

type Props = {
  messages: Message[];
  isLoading: boolean;
  onNearBottomChange?: (value: boolean) => void;
  otherUserLastReadMessageId?: string | null;
  showSeen?: boolean;
  hasMore?: boolean;
  isFetchingMore?: boolean;
  onLoadMore?: () => void;
  conversationId?: string | null;
};

function formatMessageTime(value: string) {
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function isNearBottom(element: HTMLDivElement, threshold = 120) {
  return (
    element.scrollHeight - element.scrollTop - element.clientHeight < threshold
  );
}

function hasOtherUserSeenMessage(
  messages: Message[],
  targetMessageId: string,
  otherUserLastReadMessageId?: string | null,
) {
  if (!otherUserLastReadMessageId) return false;

  const seenIndex = messages.findIndex(
    (m) => m.messageId === otherUserLastReadMessageId,
  );
  const targetIndex = messages.findIndex(
    (m) => m.messageId === targetMessageId,
  );

  if (seenIndex === -1 || targetIndex === -1) return false;

  return seenIndex >= targetIndex;
}

export function MessageList({
  messages,
  isLoading,
  onNearBottomChange,
  otherUserLastReadMessageId,
  showSeen = true,
  hasMore = false,
  isFetchingMore = false,
  onLoadMore,
  conversationId,
}: Props) {
  const { data: me } = useCurrentUser();
  const containerRef = useRef<HTMLDivElement | null>(null);

  const shouldAutoScrollRef = useRef(true);
  const previousScrollHeightRef = useRef(0);
  const waitingRestoreScrollRef = useRef(false);
  const initialScrolledConversationRef = useRef<string | null>(null);

  const orderedMessages = useMemo(() => {
    return [...(messages ?? [])].sort((a, b) => {
      const timeDiff =
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();

      if (timeDiff !== 0) return timeDiff;
      return a.messageId.localeCompare(b.messageId);
    });
  }, [messages]);

  const lastMyMessageId = useMemo(() => {
    if (!me?.userId) return null;

    for (let i = orderedMessages.length - 1; i >= 0; i -= 1) {
      if (
        orderedMessages[i].senderType === 'USER' &&
        orderedMessages[i].senderUserId === me.userId
      ) {
        return orderedMessages[i].messageId;
      }
    }

    return null;
  }, [orderedMessages, me?.userId]);

  const seenLastMyMessageId = useMemo(() => {
    if (!lastMyMessageId) return null;

    const isSeen = hasOtherUserSeenMessage(
      orderedMessages,
      lastMyMessageId,
      otherUserLastReadMessageId,
    );

    return isSeen ? lastMyMessageId : null;
  }, [orderedMessages, lastMyMessageId, otherUserLastReadMessageId]);

  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;

    const handleScroll = () => {
      const nearBottom = isNearBottom(el);
      shouldAutoScrollRef.current = nearBottom;
      onNearBottomChange?.(nearBottom);

      const nearTop = el.scrollTop <= 80;

      if (
        nearTop &&
        hasMore &&
        !isFetchingMore &&
        !waitingRestoreScrollRef.current &&
        onLoadMore
      ) {
        previousScrollHeightRef.current = el.scrollHeight;
        waitingRestoreScrollRef.current = true;
        onLoadMore();
      }
    };

    el.addEventListener('scroll', handleScroll);
    handleScroll();

    return () => {
      el.removeEventListener('scroll', handleScroll);
    };
  }, [hasMore, isFetchingMore, onLoadMore, onNearBottomChange]);

  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;

    if (waitingRestoreScrollRef.current && !isFetchingMore) {
      const newScrollHeight = el.scrollHeight;
      const heightDiff = newScrollHeight - previousScrollHeightRef.current;
      el.scrollTop = el.scrollTop + heightDiff;
      waitingRestoreScrollRef.current = false;
    }
  }, [isFetchingMore, orderedMessages.length]);

  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;
    if (!conversationId) return;
    if (initialScrolledConversationRef.current === conversationId) return;
    if (orderedMessages.length === 0) return;

    initialScrolledConversationRef.current = conversationId;
    shouldAutoScrollRef.current = true;
    waitingRestoreScrollRef.current = false;

    requestAnimationFrame(() => {
      const currentEl = containerRef.current;
      if (!currentEl) return;
      currentEl.scrollTop = currentEl.scrollHeight;
      onNearBottomChange?.(true);
    });
  }, [conversationId, orderedMessages.length, onNearBottomChange]);

  useEffect(() => {
    if (conversationId == null) {
      initialScrolledConversationRef.current = null;
      shouldAutoScrollRef.current = true;
      waitingRestoreScrollRef.current = false;
    }
  }, [conversationId]);

  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;
    if (waitingRestoreScrollRef.current) return;
    if (!shouldAutoScrollRef.current) return;

    el.scrollTop = el.scrollHeight;
  }, [orderedMessages.length]);

  if (isLoading) {
    return (
      <div className='flex h-full items-center justify-center text-sm text-slate-500'>
        Đang tải tin nhắn...
      </div>
    );
  }

  if (orderedMessages.length === 0) {
    return (
      <div className='flex h-full items-center justify-center text-sm text-slate-500'>
        Chưa có tin nhắn nào. Hãy gửi tin nhắn đầu tiên.
      </div>
    );
  }

  return (
    <div ref={containerRef} className='h-full overflow-y-auto px-4 py-4 sm:px-6 sm:py-6'>
      <div className='mx-auto flex w-full max-w-4xl flex-col gap-4'>
        {isFetchingMore && (
          <div className='text-center text-xs text-slate-400'>
            Đang tải tin nhắn cũ hơn...
          </div>
        )}

        {orderedMessages.map((message) => {
          const isAi = message.senderType === 'AI';
          const isMine = !isAi && message.senderUserId === me?.userId;
          const isSeenReceiptTarget =
            showSeen && isMine && message.messageId === seenLastMyMessageId;

          return (
            <div key={message.messageId} className='flex flex-col gap-1'>
              <MessageBubble
                content={message.content}
                createdAtLabel={formatMessageTime(message.createdAt)}
                isMine={isMine}
                isAi={isAi}
              />

              {isSeenReceiptTarget && (
                <div className='px-2 text-right text-xs text-slate-500'>
                  Đã xem
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
