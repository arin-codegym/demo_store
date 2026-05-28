'use client';

import { FormEvent, useEffect, useMemo, useRef, useState } from 'react';
import { MessageList } from './message-list';
import { useSendMessageInfinite } from '@/query/chat/use-send-message-infinite';
import { useInfiniteMessages } from '@/query/chat/use-infinite-messages';
import { useMarkReadByConversationId } from '@/query/notifications/useMarkReadByConversation';

type Props = {
  conversationId: string;
  currentUserId?: string;
  onClose: () => void;
  onOpenInMain: () => void;
};

export function AiChatWidget({
  conversationId,
  currentUserId,
  onClose,
  onOpenInMain,
}: Props) {
  const [content, setContent] = useState('');
  const [awaitingAiReply, setAwaitingAiReply] = useState(false);
  const pollingRef = useRef(false);

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetching,
    isFetchingNextPage,
    isLoading,
    refetch,
  } = useInfiniteMessages(conversationId);
  // console.log('[AiChatWidget][render]', {
  //   conversationId,
  //   content,
  //   isLoading,
  //   isFetching,
  //   isFetchingNextPage,
  //   hasNextPage,
  //   totalMessages: data?.pages.flatMap((page) => page).length ?? 0,
  // });
  const sendMessageMutation = useSendMessageInfinite(currentUserId);

  const normalizedMessages = useMemo(() => {
    return data?.pages.flatMap((page) => page) ?? [];
  }, [data]);

  const { mutate: markReadByConversation } = useMarkReadByConversationId();
  const lastMarkedMessageIdRef = useRef<string | null>(null);

  const lastMessage = normalizedMessages.length
    ? normalizedMessages[normalizedMessages.length - 1]
    : null;

  useEffect(() => {
    if (!conversationId || !lastMessage?.messageId) return;
    if (lastMarkedMessageIdRef.current === lastMessage.messageId) return;

    lastMarkedMessageIdRef.current = lastMessage.messageId;
    markReadByConversation(conversationId);
  }, [conversationId, lastMessage?.messageId, markReadByConversation]);

  useEffect(() => {
    lastMarkedMessageIdRef.current = null;
  }, [conversationId]);

  // console.log('[AiChatWidget][messages]', {
  //   conversationId,
  //   count: normalizedMessages.length,
  //   messages: normalizedMessages.map((m) => ({
  //     messageId: m.messageId,
  //     senderType: m.senderType,
  //     createdAt: m.createdAt,
  //     content: m.content,
  //   })),
  // });
  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const trimmed = content.trim();
    // console.log('[AiChatWidget][handleSubmit]', {
    //   conversationId,
    //   rawContent: content,
    //   trimmed,
    //   isPending: sendMessageMutation.isPending,
    // });
    if (!trimmed) return;
    if (sendMessageMutation.isPending) return;

    try {
      await sendMessageMutation.mutateAsync({
        conversationId,
        content: trimmed,
        clientMessageId: crypto.randomUUID(),
      });

      setAwaitingAiReply(true);
      setContent('');
    } catch (error) {
      console.error('[ai-chat] send message failed', error);
    }
  };

  useEffect(() => {
    if (!awaitingAiReply) return;
    if (lastMessage?.senderType !== 'AI') return;

    setAwaitingAiReply(false);
  }, [awaitingAiReply, lastMessage?.senderType, lastMessage?.messageId]);

  useEffect(() => {
    if (!awaitingAiReply || !conversationId) return;

    const intervalId = window.setInterval(async () => {
      if (pollingRef.current) return;

      pollingRef.current = true;
      try {
        await refetch();
      } finally {
        pollingRef.current = false;
      }
    }, 2000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [awaitingAiReply, conversationId, refetch]);

  const isSending = sendMessageMutation.isPending;
  const showAiProcessing = isSending || awaitingAiReply || isFetching;
  return (
    <div className='fixed inset-x-3 bottom-3 z-50 flex h-[min(620px,calc(100svh-7.5rem))] flex-col overflow-hidden rounded-lg border bg-white shadow-2xl sm:inset-x-auto sm:bottom-4 sm:right-4 sm:h-[600px] sm:w-[380px] sm:rounded-xl'>
      <div className='flex shrink-0 items-center justify-between gap-3 border-b px-3 py-3 sm:px-4'>
        <div className='min-w-0'>
          <div className='font-semibold'>Trợ lý AI</div>
          <div className='text-sm text-slate-500'>Hỏi đáp nhanh</div>
        </div>

        <div className='flex shrink-0 items-center gap-2'>
          <button
            type='button'
            onClick={onOpenInMain}
            className='whitespace-nowrap rounded border px-2 py-1 text-sm'
          >
            Mở lớn
          </button>

          <button
            type='button'
            onClick={onClose}
            className='whitespace-nowrap rounded border px-2 py-1 text-sm'
          >
            Đóng
          </button>
        </div>
      </div>

      <div className='min-h-0 flex-1'>
        <MessageList
          messages={normalizedMessages}
          isLoading={isLoading}
          showSeen={false}
          conversationId={conversationId}
          hasMore={!!hasNextPage}
          isFetchingMore={isFetchingNextPage}
          onLoadMore={() => {
            if (hasNextPage && !isFetchingNextPage) {
              fetchNextPage();
            }
          }}
        />
      </div>

      <div className='shrink-0 border-t p-2 sm:p-3'>
        <form onSubmit={handleSubmit} className='flex gap-2'>
          <input
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder='Nhập câu hỏi cho AI...'
            disabled={sendMessageMutation.isPending}
            className='min-w-0 flex-1 rounded border px-3 py-2 text-sm outline-none'
          />

          <button
            type='submit'
            disabled={sendMessageMutation.isPending || !content.trim()}
            className='shrink-0 rounded border px-3 py-2 text-sm disabled:opacity-50'
          >
            {sendMessageMutation.isPending ? 'Đang gửi...' : 'Gửi'}
          </button>
        </form>

        {showAiProcessing && (
          <div className='mt-2 text-xs text-slate-400'>AI đang xử lý...</div>
        )}
      </div>
    </div>
  );
}
