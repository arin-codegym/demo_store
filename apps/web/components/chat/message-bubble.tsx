'use client';

import type { Message } from '@/utils/types';

type MessageBubbleProps = {
  content: string;
  createdAtLabel: string;
  isMine: boolean;
  isAi: boolean;
};

function formatDateTime(value: string) {
  const date = new Date(value);
  return date.toLocaleString();
}

export function MessageBubble({
  content,
  createdAtLabel,
  isMine,
  isAi,
}: MessageBubbleProps) {
  const alignClass = isMine ? 'items-end' : 'items-start';

  const bubbleClass = isMine
    ? 'bg-sky-600 text-white'
    : isAi
      ? 'bg-violet-100 text-slate-900'
      : 'bg-slate-100 text-slate-900';

  const senderLabel = isAi ? 'AI' : null;

  return (
    <div className={`flex flex-col ${alignClass}`}>
      {senderLabel && (
        <div className='mb-1 px-1 text-xs text-slate-500'>{senderLabel}</div>
      )}

      <div className={`max-w-[75%] rounded-2xl px-4 py-2 ${bubbleClass}`}>
        <div className='whitespace-pre-wrap break-words'>{content}</div>
      </div>

      <div className='mt-1 px-1 text-xs text-slate-400'>{createdAtLabel}</div>
    </div>
  );
}
