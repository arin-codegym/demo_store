import { NextRequest, NextResponse } from 'next/server';

export const POST = async (req: NextRequest) => {
  const body = await req.text();

  const res = await fetch(
    `${process.env.API_EXTERNAL}/notifications/mark-read-by-conversation`,
    {
      method: 'POST',
      headers: {
        cookie: req.headers.get('cookie') || '',
        'Content-Type': 'application/json',
      },
      body,
    },
  );

  // console.log('[route] backend status =', res.status);

  const raw = await res.text();
  // console.log('[route] backend raw =', raw);

  return new NextResponse(raw, {
    status: res.status,
    headers: {
      'Content-Type': res.headers.get('Content-Type') || 'application/json',
    },
  });
};
