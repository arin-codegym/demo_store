'use client';

import { useState } from 'react';
import { useSendMessage } from '@/query/chat/use-send-message';

type Props = {
  conversationId: string;
};

function createClientMessageId() {
  return crypto.randomUUID();
}

export function MessageComposer({ conversationId }: Props) {
  const [content, setContent] = useState('');
  const sendMessageMutation = useSendMessage();

  async function handleSend() {
    const value = content.trim();
    if (!value) return;

    await sendMessageMutation.mutateAsync({
      conversationId,
      clientMessageId: createClientMessageId(),
      content: value,
    });

    setContent('');
  }

  return (
    <div className='border-t border-slate-200 bg-white px-4 py-3'>
      <div className='mx-auto flex w-full max-w-4xl items-end gap-3'>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder='Nhập tin nhắn...'
          rows={1}
          className='h-11 flex-1 rounded-xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none'
          onKeyDown={async (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
              e.preventDefault();
              await handleSend();
            }
          }}
        />

        <button
          type='button'
          onClick={handleSend}
          disabled={sendMessageMutation.isPending}
          className='h-11 rounded-xl bg-slate-900 px-5 text-sm font-medium text-white'
        >
          Send
        </button>
      </div>
    </div>
  );
}
