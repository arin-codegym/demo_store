import 'server-only';
import { cookies } from 'next/headers';

export async function fetchMeServer() {
  const cookieStore = await cookies();
  const cookieHeader = cookieStore.toString();

  let res = await fetch(`${process.env.API_EXTERNAL}/auth/me`, {
    headers: { Cookie: cookieHeader },
    cache: 'no-store',
  });

  // accessToken expired
  if (res.status === 401) {
    const refreshRes = await fetch(
      `${process.env.NEXT_PUBLIC_APP_URL}/api/auth/refresh`,
      {
        method: 'POST',
        headers: {
          cookie: cookieHeader,
        },
        cache: 'no-store',
      },
    );

    if (!refreshRes.ok) return null;

    // 🔥 sau khi refresh, gọi lại /me
    // retry
    res = await fetch(`${process.env.API_EXTERNAL}/auth/me`, {
      headers: { Cookie: cookieHeader },
      cache: 'no-store',
    });
  }

  if (!res.ok) return null;

  return res.json();
}
