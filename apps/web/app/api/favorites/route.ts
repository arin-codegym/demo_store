import { NextRequest, NextResponse } from 'next/server';
import { fetchWithAuthServer } from '@/lib/fetchWithAuth.server';

//app\api\favorites\route.ts
export async function GET(req: NextRequest) {
  const cookieHeader = req.headers.get('cookie') ?? '';
  //   const payload = await req.json();
  return fetch(`${process.env.API_EXTERNAL}/favorites/fetchUserFavorites`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      cookie: cookieHeader,
      accept: 'application/json',
    },
  });
}
