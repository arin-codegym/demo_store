import { NextRequest, NextResponse } from 'next/server';
import {
  buildBackendProxyHeaders,
  toProxyResponse,
} from '@/lib/api/backend-proxy-headers';

export const GET = async (req: NextRequest) => {
  const { searchParams } = new URL(req.url);
  const assistantCode = searchParams.get('assistantCode') || 'general';

  try {
    const upstream = await fetch(
      `${process.env.API_EXTERNAL}/conversations/ai/me?assistantCode=${encodeURIComponent(assistantCode)}`,
      {
        method: 'GET',
        headers: buildBackendProxyHeaders(req),
        cache: 'no-store',
      },
    );

    return toProxyResponse(upstream);
  } catch (error) {
    console.error('[api/user/conversations/ai/me] proxy failed', error);
    return NextResponse.json(
      { message: 'Failed to open AI conversation' },
      { status: 502 },
    );
  }
};
