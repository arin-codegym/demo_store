import { Headset } from 'lucide-react';
import { Button } from '../ui/button';

type AdminChatLauncherProps = {
  onClick: () => void;
  disabled?: boolean;
};

export function AdminChatLauncher({
  onClick,
  disabled,
}: AdminChatLauncherProps) {
  return (
    <Button
      onClick={onClick}
      disabled={disabled}
      className='fixed bottom-4 right-4 z-40 h-12 rounded-full bg-slate-900 px-4 text-white shadow-lg transition hover:scale-105 disabled:opacity-60'
      aria-label='Chat with admin'
    >
      <Headset size={18} />
      <span>Admin</span>
    </Button>
  );
}
