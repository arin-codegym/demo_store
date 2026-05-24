import { headers } from 'next/headers';

export async function POST() {
  const headersList = await headers();
  const cookieHeader = headersList.get('cookie') ?? '';
  const backendRes = await fetch(`${process.env.API_EXTERNAL}/auth/refresh`, {
    method: 'GET',
    headers: {
      cookie: cookieHeader,
    },
  });

  // const res = NextResponse.json({ ok: true });

  // const setCookie = backendRes.headers.get('set-cookie');
  // if (setCookie) {
  //   res.headers.set('set-cookie', setCookie);
  // }

  // return res;
  return new Response(backendRes.body, {
    status: backendRes.status,
    headers: backendRes.headers,
  });
}
