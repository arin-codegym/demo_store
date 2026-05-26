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
      className='fixed bottom-24 right-4 z-40 rounded-full border bg-white px-4 py-3 shadow'
    >
      {loading ? 'Đang mở...' : 'Chat AI'}
    </button>
  );
}
