import { NextRequest, NextResponse } from 'next/server';
import {
  buildBackendProxyHeaders,
  toProxyResponse,
} from '@/lib/api/backend-proxy-headers';

export const GET = async (req: NextRequest) => {
  try {
    const upstream = await fetch(
      `${process.env.API_EXTERNAL}/conversations/admin/me`,
      {
        method: 'GET',
        headers: buildBackendProxyHeaders(req),
        cache: 'no-store',
      },
    );

    return toProxyResponse(upstream);
  } catch (error) {
    console.error('[api/user/conversations/admin/me] proxy failed', error);
    return NextResponse.json(
      { message: 'Failed to open admin conversation' },
      { status: 502 },
    );
  }
};
