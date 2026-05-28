'use client';

type Props = {
  onClick: () => void;
  disabled?: boolean;
  loading?: boolean;
};

export function AiChatLauncher({ onClick, disabled, loading }: Props) {
  return (
    <button
      type='button'
      onClick={onClick}
      disabled={disabled}
      className='fixed bottom-[5.25rem] right-4 z-40 rounded-full border bg-white px-4 py-3 text-sm shadow transition hover:bg-slate-50 disabled:opacity-60 sm:bottom-24'
    >
      {loading ? 'Đang mở...' : 'Chat AI'}
    </button>
  );
}
