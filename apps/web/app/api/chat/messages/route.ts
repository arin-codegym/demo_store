import { NextRequest, NextResponse } from 'next/server';
import {
  buildBackendProxyHeaders,
  toProxyResponse,
} from '@/lib/api/backend-proxy-headers';

export const POST = async (req: NextRequest) => {
  try {
    const body = await req.json();
    const upstream = await fetch(`${process.env.API_EXTERNAL}/messages`, {
      method: 'POST',
      headers: buildBackendProxyHeaders(req, { json: true }),
      body: JSON.stringify(body),
      cache: 'no-store',
    });

    return toProxyResponse(upstream);
  } catch (error) {
    console.error('[api/chat/messages] proxy failed', error);
    return NextResponse.json(
      { message: 'Failed to send message' },
      { status: 502 },
    );
  }
};
