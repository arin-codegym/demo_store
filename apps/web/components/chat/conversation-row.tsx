'use client';

import { useMarkReadByConversationId } from '@/query/notifications/useMarkReadByConversation';
import {
  chatUiActions,
  useChatUiReduxDispatch,
  useChatUiReduxSelector,
} from '@/stores/chat-ui-redux-store';
import type { ConversationSummary } from '@/utils/types';
import { useEffect, useRef } from 'react';

type Props = {
  conversation: ConversationSummary;
};

function formatTime(value?: string | null) {
  if (!value) return '';
  const date = new Date(value);
  return date.toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function ConversationRow({ conversation }: Props) {
  const dispatch = useChatUiReduxDispatch();
  // Zustand: useChatUiStore((s) => s.selectedConversationId)
  const selectedConversationId = useChatUiReduxSelector(
    (state) => state.chatUi.selectedConversationId,
  );
  // Zustand: useChatUiStore((s) => s.setSelectedConversationId)
  const setSelectedConversationId = (conversationId: string | null) =>
    dispatch(chatUiActions.setSelectedConversationId(conversationId));

  const active = selectedConversationId === conversation.conversationId;
  const initial = conversation.otherUserName?.charAt(0)?.toUpperCase() ?? 'U';
  const showUnreadBadge = conversation.unreadCount > 0 && !active;
  const { mutate: markReadByConversation } = useMarkReadByConversationId();
  const lastMarkedMainChatRef = useRef<string | null>(null);

  useEffect(() => {
    if (!selectedConversationId) return;

    const markKey = `${selectedConversationId}`;
    if (lastMarkedMainChatRef.current === markKey) return;

    lastMarkedMainChatRef.current = markKey;

    markReadByConversation(selectedConversationId);
  }, [selectedConversationId, markReadByConversation]);

  return (
    <button
      type='button'
      onClick={() => setSelectedConversationId(conversation.conversationId)}
      className={[
        'w-full rounded-xl px-3 py-3 text-left transition',
        'border border-transparent',
        active
          ? 'bg-slate-100 border-slate-200 shadow-sm'
          : 'hover:bg-slate-50',
      ].join(' ')}
    >
      <div className='flex items-center gap-3'>
        <div className='flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-slate-200 text-sm font-semibold text-slate-700'>
          {initial}
        </div>

        <div className='min-w-0 flex-1'>
          <div className='flex items-start justify-between gap-3'>
            <div className='min-w-0'>
              <div className='truncate text-sm font-semibold text-slate-900'>
                {conversation.otherUserName}
              </div>
            </div>

            <div className='shrink-0 pt-0.5 text-xs text-slate-400'>
              {formatTime(conversation.lastMessageAt)}
            </div>
          </div>

          <div className='mt-1 flex items-center justify-between gap-2'>
            <div className='truncate text-sm text-slate-500'>
              {conversation.lastMessageContent ?? 'Chưa có tin nhắn'}
            </div>

            {showUnreadBadge && (
              <div className='flex h-5 min-w-5 shrink-0 items-center justify-center rounded-full bg-slate-900 px-1.5 text-[11px] font-semibold text-white'>
                {conversation.unreadCount}
              </div>
            )}
          </div>
        </div>
      </div>
    </button>
  );
}
