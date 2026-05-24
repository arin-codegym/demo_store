'use client';

import { useCurrentUser } from './useCurrentUser';

export function useAuth() {
  const meQuery = useCurrentUser();

  return {
    me: meQuery.data ?? null,
    isAuthenticated: !!meQuery.data,
    isLoading: meQuery.isLoading,
    isError: meQuery.isError,
    error: meQuery.error,
    refetchMe: meQuery.refetch,
  };
}
