'use client';

import { useEffect, useState } from 'react';
import { useCreateDirectConversation } from '@/query/chat/use-create-direct-conversation';
import { useChatableUsers } from '@/query/chat/use-chatable-users';
import {
  chatUiActions,
  useChatUiReduxDispatch,
} from '@/stores/chat-ui-redux-store';

type Props = {
  open: boolean;
  onClose: () => void;
};

export function NewChatModal({ open, onClose }: Props) {
  const [keyword, setKeyword] = useState('');
  const [debouncedKeyword, setDebouncedKeyword] = useState('');
  const { data: users = [], isLoading } = useChatableUsers(
    debouncedKeyword,
    open,
  );
  const createDirectConversation = useCreateDirectConversation();
  const dispatch = useChatUiReduxDispatch();
  // Zustand: const setSelectedConversationId = useChatUiStore((s) => s.setSelectedConversationId)
  const setSelectedConversationId = (conversationId: string | null) =>
    dispatch(chatUiActions.setSelectedConversationId(conversationId));
  useEffect(() => {
    if (!open) {
      setKeyword('');
    }
  }, [open]);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedKeyword(keyword);
    }, 300);

    return () => clearTimeout(timer);
  }, [keyword]);

  if (!open) return null;

  // async function handleCreate(targetUserId: string) {
  //   const result = await createDirectConversation.mutateAsync({ targetUserId });
  //   setSelectedConversationId(result?.conversationId ?? null);
  //   onClose();
  // }
  async function handleCreate(targetUserId: string) {
    try {
      const result = await createDirectConversation.mutateAsync({
        targetUserId,
      });
      if (!result?.conversationId) {
        throw new Error('Conversation id is missing');
      }

      setSelectedConversationId(result.conversationId);
      onClose();
    } catch (error) {
      console.error('Create direct conversation failed', error);
      alert(error);
    }
  }

  return (
    <div className='fixed inset-0 z-50 flex items-center justify-center bg-black/30 px-4'>
      <div className='w-full max-w-md rounded-xl bg-white shadow-xl'>
        <div className='border-b px-4 py-3'>
          <div className='text-lg font-semibold'>New chat</div>
          <div className='text-sm text-gray-500'>
            Tìm user để bắt đầu conversation
          </div>
        </div>

        <div className='p-4'>
          <input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder='Search user...'
            className='mb-4 w-full rounded-md border px-3 py-2'
          />

          <div className='max-h-80 overflow-y-auto rounded-md border'>
            {isLoading && <div className='p-3 text-sm'>Loading users...</div>}

            {!isLoading && users?.length === 0 && (
              <div className='p-3 text-sm text-gray-500'>
                Không tìm thấy user
              </div>
            )}

            {!isLoading &&
              users?.map((user) => (
                <button
                  key={user.userId}
                  type='button'
                  onClick={() => handleCreate(user.userId)}
                  className='flex w-full items-center justify-between border-b px-3 py-3 text-left last:border-b-0 hover:bg-gray-50'
                >
                  <div>
                    <div className='font-medium'>{user?.username}</div>
                    <div className='text-sm text-gray-500'>{user.userId}</div>
                  </div>
                </button>
              ))}
          </div>
        </div>

        <div className='flex justify-end gap-2 border-t px-4 py-3'>
          <button
            type='button'
            onClick={onClose}
            className='rounded-md border px-3 py-2 text-sm'
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
