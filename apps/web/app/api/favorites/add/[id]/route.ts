import { NextRequest, NextResponse } from 'next/server';

export async function POST(
  req: NextRequest,
  context: { params: Promise<{ id: string }> },
) {
  const payload = await req.text();
  return fetch(`${process.env.API_EXTERNAL}/favorites`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      cookie: req.headers.get('cookie') ?? '',
      accept: 'application/json',
    },
    body: payload,
  });
}
