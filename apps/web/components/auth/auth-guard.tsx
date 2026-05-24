'use client';

import { ReactNode, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useCurrentUser } from '@/query/auth/useCurrentUser';

export function AuthGuard({ children }: { children: ReactNode }) {
  const { data: me, isLoading, isError, error } = useCurrentUser();

  useEffect(() => {
    if (isLoading) return;

    // ví dụ error là 401 => thử bootstrap
    const status = (error as any)?.response?.status ?? (error as any)?.status;

    if (status === 401) {
      window.location.assign('/api/auth/bootstrap?next=/chat');
      return;
    }

    if (isError || !me) {
      window.location.assign('/login');
    }
  }, [isLoading, isError, me, error]);

  if (isLoading) {
    return <div className='p-4'>Loading...</div>;
  }

  if (isError || !me) {
    return null;
  }

  return <>{children}</>;
}
