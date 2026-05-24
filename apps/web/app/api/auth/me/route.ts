import { cookies } from 'next/headers';
import { NextResponse } from 'next/server';

export async function GET() {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get('accessToken')?.value;
  // if (accessToken) return new NextResponse();
  if (!accessToken) return new NextResponse();
  const res = await fetch(`${process.env.API_EXTERNAL}/auth/me`, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  if (!res.ok) return Response.json({}, { status: 401 });

  const data = await res.json();
  return Response.json(data);
}
