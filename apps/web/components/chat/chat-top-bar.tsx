'use client';

import { useCurrentUser } from '@/query/auth/useCurrentUser';

export function ChatTopbar() {
  const { data: me } = useCurrentUser();

  return (
    <div className='border-b border-slate-200 bg-white'>
      <div className='mx-auto flex h-16 max-w-7xl items-center justify-between px-4'>
        <div>
          <div className='text-lg font-semibold text-slate-900'>Messages</div>
          <div className='text-xs text-slate-500'>Realtime chat workspace</div>
        </div>

        <div className='hidden w-full max-w-md px-6 md:block'>
          <input
            type='text'
            placeholder='Search conversations...'
            className='w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2 text-sm outline-none transition focus:border-slate-300'
          />
        </div>

        <div className='flex items-center gap-3'>
          <div className='hidden text-right sm:block'>
            <div className='text-sm font-medium text-slate-900'>
              {me?.fullName ?? 'User'}
            </div>
            <div className='text-xs text-slate-500'>{me?.email ?? ''}</div>
          </div>

          <div className='flex h-10 w-10 items-center justify-center rounded-full bg-slate-900 text-sm font-semibold text-white'>
            {me?.fullName?.charAt(0)?.toUpperCase() ?? 'U'}
          </div>
        </div>
      </div>
    </div>
  );
}
