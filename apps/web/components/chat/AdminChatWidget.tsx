'use client';
import { X, Minus } from 'lucide-react';
import { Button } from '../ui/button';
import { useInfiniteMessages } from '../../query/chat/use-infinite-messages';

import { useEffect, useMemo, useRef, useState } from 'react';
import { useSendMessage } from '@/query/chat/use-send-message';
import { useSendMessageInfinite } from '@/query/chat/use-send-message-infinite';
import { MessageList } from './message-list';
import { useConversationReadState } from '@/query/chat/use-conversation-read-state';
import { useMarkReadByConversationId } from '@/query/notifications/useMarkReadByConversation';

type AdminChatWidgetProps = {
  conversationId: string;
  onClose: () => void;
  onOpenInMain: () => void;
  currentUserId?: string;
};

export function AdminChatWidget({
  conversationId,
  onClose,
  onOpenInMain,
  currentUserId,
}: AdminChatWidgetProps) {
  const [input, setInput] = useState('');
  const { mutate: markReadByConversation } = useMarkReadByConversationId();
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } =
    useInfiniteMessages(conversationId);
  const { data: readState } = useConversationReadState(conversationId);

  const sendMessageMutation = useSendMessageInfinite(currentUserId);

  const messages = useMemo(() => {
    const all = data?.pages.flatMap((page) => page ?? []) ?? [];
    const map = new Map(all.map((m) => [m.messageId, m]));
    return Array.from(map.values()).sort(
      (a, b) =>
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
    );
  }, [data]);
  // console.log('readState =', readState);
  const lastMessage = messages.length ? messages[messages.length - 1] : null;
  const lastMarkedMessageIdRef = useRef<string | null>(null);

  useEffect(() => {
    if (!conversationId || !lastMessage?.messageId) return;

    if (lastMarkedMessageIdRef.current === lastMessage.messageId) return;

    lastMarkedMessageIdRef.current = lastMessage.messageId;
    markReadByConversation(conversationId);
  }, [
    conversationId,
    lastMessage?.messageId,
    markReadByConversation,
    currentUserId,
  ]);

  useEffect(() => {
    lastMarkedMessageIdRef.current = null;
  }, [conversationId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const content = input.trim();
    if (!content || sendMessageMutation.isPending) return;

    try {
      await sendMessageMutation.mutateAsync({
        conversationId,
        content: content,
        clientMessageId: crypto.randomUUID(),
      });
      setInput('');
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div className='fixed inset-x-3 bottom-3 z-50 flex h-[min(560px,calc(100svh-7.5rem))] flex-col overflow-hidden rounded-lg border bg-white shadow-2xl sm:inset-x-auto sm:bottom-20 sm:right-4 sm:h-[520px] sm:w-[360px] sm:rounded-2xl'>
      <div className='flex h-16 shrink-0 items-center justify-between gap-3 border-b px-3 sm:h-14 sm:px-4'>
        <div className='min-w-0'>
          <div className='font-semibold'>Admin Support</div>
          <div className='text-xs text-slate-500'>Liên hệ hỗ trợ</div>
        </div>

        <div className='flex shrink-0 items-center gap-2'>
          <button
            type='button'
            onClick={onOpenInMain}
            className='whitespace-nowrap rounded-lg border px-2 py-1 text-xs'
          >
            Mở lớn
          </button>
          <button
            type='button'
            onClick={onClose}
            className='whitespace-nowrap rounded-lg border px-2 py-1 text-xs'
          >
            Đóng
          </button>
        </div>
      </div>

      <div className='min-h-0 flex-1'>
        <MessageList
          messages={messages}
          isLoading={isLoading}
          hasMore={!!hasNextPage}
          isFetchingMore={isFetchingNextPage}
          onLoadMore={() => fetchNextPage()}
          conversationId={conversationId}
          otherUserLastReadMessageId={
            readState?.otherUserLastReadMessageId ?? null
          }
        />
      </div>

      <form onSubmit={handleSubmit} className='shrink-0 border-t p-2 sm:p-3'>
        <div className='flex items-end gap-2'>
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder='Nhập nội dung hỗ trợ...'
            className='min-h-[44px] max-h-28 min-w-0 flex-1 resize-none rounded-xl border px-3 py-2 text-sm outline-none'
          />
          <button
            type='submit'
            disabled={sendMessageMutation.isPending}
            className='rounded-xl bg-slate-900 px-4 py-2 text-sm text-white disabled:opacity-60'
          >
            Gửi
          </button>
        </div>
      </form>
    </div>
  );
}
