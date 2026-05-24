'use server';
import { cookies, headers } from 'next/headers';

export async function authFetch(url: string, options: RequestInit = {}) {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get('accessToken')?.value;

  return fetch(`${process.env.API_EXTERNAL}${url}`, {
    ...options,
    headers: {
      ...options.headers,
      Authorization: `Bearer ${accessToken}`,
    },
    cache: 'no-store',
  });
}
