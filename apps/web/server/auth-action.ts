'use server';
import { cookies } from 'next/headers';

export const logoutAction = async () => {
  const cookieStore = await cookies();

  // These cookies are httpOnly, so logout must clear them from a server action.
  cookieStore.delete('accessToken');
  cookieStore.delete('refreshToken');
  cookieStore.delete('sid');

  return { success: true };
};
