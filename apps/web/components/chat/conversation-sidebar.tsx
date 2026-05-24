'use client';

import { useConversations } from '@/query/chat/use-conversations';
import { ConversationRow } from './conversation-row';
import { useCurrentUser } from '@/query/auth/useCurrentUser';

type Props = {
  onOpenNewChat: () => void;
};

export function ConversationSidebar({ onOpenNewChat }: Props) {
  const { data: me } = useCurrentUser();
  const { data: conversations = [], isLoading } = useConversations();
  return (
    <aside className='h-full overflow-y-auto border-r'>
      <div className='border-b border-slate-200 p-5'>
        <div className='mb-4 flex items-center gap-3'>
          <div className='flex h-11 w-11 items-center justify-center rounded-full bg-slate-900 text-sm font-semibold text-white'>
            {me?.fullName?.charAt(0)?.toUpperCase() ?? 'U'}
          </div>

          <div className='min-w-0'>
            <div className='truncate font-semibold text-slate-900'>
              {me?.fullName ?? 'User'}
            </div>
            <div className='truncate text-sm text-slate-500'>
              {me?.email ?? ''}
            </div>
          </div>
        </div>

        <button
          type='button'
          onClick={onOpenNewChat}
          className='w-full rounded-xl bg-slate-900 px-4 py-3 text-sm font-medium text-white transition hover:bg-slate-800'
        >
          + New chat
        </button>
      </div>

      <div className='min-h-0 flex-1 overflow-y-auto p-2'>
        {isLoading && (
          <div className='rounded-xl px-3 py-4 text-sm text-slate-500'>
            Loading conversations...
          </div>
        )}

        {!isLoading && conversations?.length === 0 && (
          <div className='rounded-xl px-3 py-4 text-sm text-slate-500'>
            Chưa có conversation nào. Hãy tạo chat mới.
          </div>
        )}

        {!isLoading && (
          <div className='space-y-1'>
            {conversations.map((conversation) => (
              <ConversationRow
                key={conversation.conversationId}
                conversation={conversation}
              />
            ))}
          </div>
        )}
      </div>
    </aside>
  );
}
