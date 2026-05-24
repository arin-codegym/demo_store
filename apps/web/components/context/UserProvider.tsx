'use client';

import { User } from '@/utils/types';
import { createContext, useContext } from 'react';

const UserContext = createContext<User | null | undefined>(undefined);

export function UserProvider({
  user,
  children,
}: {
  user: User | null | undefined;
  children: React.ReactNode;
}) {
  return <UserContext.Provider value={user}>{children}</UserContext.Provider>;
}
export function useUser() {
  return useContext(UserContext);
}
