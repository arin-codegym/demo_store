// app/api/auth/bootstrap/route.ts
import { NextRequest, NextResponse } from 'next/server';

export const runtime = 'nodejs'; // tránh edge để xử lý set-cookie ổn định hơn
/* Đây là pattern hợp lệ: server-side refresh + redirect. */
export async function GET(req: NextRequest) {
  const url = new URL(req.url);
  const next = getSafeRedirectPath(url.searchParams.get('next'));
  const redirectBaseUrl = getRedirectBaseUrl(req);
  // ✅ forward ALL cookies (refreshToken + sid + ...)
  const cookieHeader = req.headers.get('cookie');
  if (!cookieHeader) {
    const res = NextResponse.redirect(new URL('/', redirectBaseUrl));
    res.cookies.delete('accessToken');
    res.cookies.delete('refreshToken');
    res.cookies.delete('sid');
    return res;
  }
  // Gọi Spring refresh (server-to-server)
  const refreshRes = await fetch(`${process.env.API_EXTERNAL}/auth/refresh`, {
    method: 'GET',
    headers: { Cookie: cookieHeader },
    cache: 'no-store',
  });
  // const data = await refreshRes.json();

  if (!refreshRes.ok) {
    const res = NextResponse.redirect(new URL('/', redirectBaseUrl));

    res.cookies.set('accessToken', '', {
      httpOnly: true,
      path: '/',
      maxAge: 0,
      sameSite: 'lax',
    });

    res.cookies.set('refreshToken', '', {
      httpOnly: true,
      path: '/',
      maxAge: 0,
      sameSite: 'lax',
    });

    res.cookies.set('sid', '', {
      httpOnly: true,
      path: '/',
      maxAge: 0,
      sameSite: 'lax',
    });

    return res;
  }

  // Trả về redirect về trang user muốn vào
  const res = NextResponse.redirect(new URL(next, redirectBaseUrl));

  // Quan trọng: backend có thể trả NHIỀU Set-Cookie (access + refresh)
  // Node fetch (undici) thường có getSetCookie()
  const anyHeaders = refreshRes.headers as any;
  const setCookies: string[] = (
    typeof anyHeaders.getSetCookie === 'function'
      ? anyHeaders.getSetCookie()
      : [refreshRes.headers.get('set-cookie')].filter(Boolean)
  ) as string[];
  // console.log('setCookies =', setCookies);
  // console.log('setCookies.length =', setCookies.length);

  for (const c of setCookies) {
    res.headers.append('set-cookie', c);
  }

  return res;
}

const getRedirectBaseUrl = (req: NextRequest) => {
  if (process.env.FRONTEND_URL) {
    return process.env.FRONTEND_URL;
  }

  const forwardedHost = req.headers.get('x-forwarded-host');
  const forwardedProto = req.headers.get('x-forwarded-proto') ?? 'https';

  if (forwardedHost) {
    return `${forwardedProto}://${forwardedHost}`;
  }

  return req.nextUrl.origin;
};

const getSafeRedirectPath = (next: string | null) => {
  if (!next || !next.startsWith('/') || next.startsWith('//')) {
    return '/';
  }

  return next;
};
