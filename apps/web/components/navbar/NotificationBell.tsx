'use client';

import { useState } from 'react';
import { useUnreadNotificationCount } from '@/query/notifications/useUnreadNotificationCount';
import { NotificationDropdown } from './NotificationDropdown';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from '../ui/dropdown-menu';
import { Button } from '../ui/button';
import { Bell } from 'lucide-react';
import { useCurrentUser } from '@/query/auth/useCurrentUser';

export function NotificationBell() {
  const [open, setOpen] = useState(false);
  const { data: currentUser } = useCurrentUser();
  const { data } = useUnreadNotificationCount(Boolean(currentUser));

  const unreadCount = data?.unreadCount ?? 0;

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant='ghost' size='icon' className='relative h-9 w-9 sm:h-10 sm:w-10'>
          <Bell className='h-5 w-5' />
          <span className='sr-only'>Mở thông báo</span>

          {unreadCount > 0 && (
            <span className='absolute -right-1 -top-1 min-w-5 rounded-full bg-red-500 px-1 text-center text-xs text-white'>
              {unreadCount > 99 ? '99+' : unreadCount}
            </span>
          )}
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent align='end' className='w-[360px] p-0'>
        <NotificationDropdown onItemClick={() => setOpen(false)} />
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
