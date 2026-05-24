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
      className='fixed bottom-4 right-4 z-40 h-14 w-14 rounded-full bg-slate-900 text-white shadow-lg hover:scale-105 transition'
      aria-label='Chat with admin'
    >
      <div className='flex items-center justify-center'>
        <Headset size={22} />
      </div>
    </Button>
  );
}
