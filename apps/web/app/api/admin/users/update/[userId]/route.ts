//\api\admin\users\update\[userId]\route.ts
import { fetchWithAuthServer } from '@/lib/fetchWithAuth.server';
import { NextRequest, NextResponse } from 'next/server';
export async function PUT(
  req: NextRequest,
  { params }: { params: Promise<{ userId: string }> },
) {
  const { userId } = await params; // ✅ userId ở đây
  const payload = await req.json();
  const cookieHeader = req.headers.get('cookie') || '';
  const pathname = new URL(req.url).pathname;
  const result = await fetch(
    `${process.env.API_EXTERNAL}/admin/users/${userId}`,
    {
      method: 'PUT',
      headers: {
        // quan trọng nhất:
        'Content-Type': 'application/json',
        cookie: cookieHeader,
        accept: 'application/json',
      },
      body: JSON.stringify(payload),
    },
  );
  // parse body
  const ct = result.headers.get('content-type') || '';
  const data = ct.includes('application/json')
    ? await result.json()
    : await result.text();

  const res = NextResponse.json(data, { status: result.status });

  return res;
}
