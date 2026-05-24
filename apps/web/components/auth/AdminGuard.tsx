'use client';
import { useCurrentUser } from '@/query/auth/useCurrentUser';
import { useDebugRouter } from '@/query/useDebugRouter';
import { useEffect } from 'react';

export function AdminGuard({ children }: { children: React.ReactNode }) {
  const { data: user, isLoading, isFetching, isFetched } = useCurrentUser();
  // const router = useRouter();
  const router = useDebugRouter();

  useEffect(() => {
    if (isLoading || isFetching) return;
    // chưa kết luận gì thì chưa redirect
    if (typeof user === 'undefined') return;
    // đã kết luận là không có user
    if (user === null) {
      router.replace('/login');
      return;
    }
    if (!user.roles?.includes('ROLE_ADMIN')) {
      router.replace('/');
      return;
    }
  }, [user, isLoading, isFetching, router]);

  return <>{children}</>;
}
