import { NextRequest, NextResponse } from 'next/server';

export async function GET(req: NextRequest) {
  const token = req.nextUrl.searchParams.get('token');

  if (!token) {
    return NextResponse.json(
      { message: 'Activation token is required' },
      { status: 400 },
    );
  }

  const upstream = await fetch(
    `${process.env.API_EXTERNAL}/auth/activate?${new URLSearchParams({
      token,
    })}`,
    {
      method: 'GET',
      headers: {
        accept: 'application/json',
      },
    },
  );

  const data = await upstream.json().catch(() => ({}));
  return NextResponse.json(data, { status: upstream.status });
}
