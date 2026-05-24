import { NextRequest, NextResponse } from 'next/server';
import { fetchJsonWithAuth } from '../fetchWithAuth.client';
import { User } from '@/utils/types';

export const getMe = async (): Promise<User | null> => {
  try {
    return await fetchJsonWithAuth<User | null>('/api/auth/me');
  } catch (error) {
    return null;
  }
};
// export const getMe = async () => {
//   return fetchJsonWithAuth<User | null>('/api/auth/me').catch(() => null);
// };
