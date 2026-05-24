import { NextRequest, NextResponse } from 'next/server';
import { fetchWithAuthServer } from '@/lib/fetchWithAuth.server';
//app/api/admin/orders/route.ts
export async function GET(req: NextRequest) {
  const cookieHeader = req.headers.get('cookie') || '';

  return fetch(`${process.env.API_EXTERNAL}/admin/orders`, {
    method: 'GET',
    headers: {
      cookie: cookieHeader,
    },
  });
}
