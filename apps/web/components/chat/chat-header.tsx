'use client';

import { useMemo } from 'react';
import { useConversations } from '@/query/chat/use-conversations';
import { useChatUiReduxSelector } from '@/stores/chat-ui-redux-store';
import { useLogout } from '@/query/auth/use-logout';

export function ChatHeader() {
  // Zustand: useChatUiStore((s) => s.selectedConversationId)
  const selectedConversationId = useChatUiReduxSelector(
    (state) => state.chatUi.selectedConversationId,
  );
  const { data: conversations = [] } = useConversations();
  const { logout } = useLogout();

  const selectedConversation = useMemo(
    () =>
      conversations?.find(
        (item) => item.conversationId === selectedConversationId,
      ) ?? null,
    [conversations, selectedConversationId],
  );

  return (
    <div className='border-b border-slate-200 bg-white px-5 py-4'>
      <div className='flex items-center gap-3'>
        <div className='flex h-10 w-10 items-center justify-center rounded-full bg-slate-200 text-sm font-semibold text-slate-700'>
          {selectedConversation ? (
            <div className='min-w-0'>
              <div className='truncate font-semibold text-slate-900'>
                {selectedConversation.otherUserName}
              </div>
              <div className='truncate text-sm text-slate-500'>
                {selectedConversation.otherUserId}
              </div>
            </div>
          ) : (
            <div className='text-sm text-gray-500'>
              Chưa chọn cuộc trò chuyện
            </div>
          )}
        </div>

        <button
          type='button'
          onClick={logout}
          className='rounded-md border px-3 py-2 text-sm hover:bg-gray-50'
        >
          Logout
        </button>
      </div>
    </div>
  );
}
