import { NextRequest, NextResponse } from 'next/server';
import { buildBackendProxyHeaders } from '@/lib/api/backend-proxy-headers';

const BACKEND_URL = process.env.API_EXTERNAL;
const REQUEST_TIMEOUT_MS = 15000;

function jsonResponse(body: unknown, status = 200) {
  return NextResponse.json(body, {
    status,
    headers: {
      'Cache-Control': 'no-store',
    },
  });
}

export async function POST(req: NextRequest) {
  try {
    if (!BACKEND_URL) {
      return jsonResponse({ message: 'Backend URL is not configured' }, 500);
    }

    const key = req.headers.get('idempotency-key');
    const headers = buildBackendProxyHeaders(req);
    if (key) headers.set('Idempotency-Key', key);

    const backendRes = await fetch(`${BACKEND_URL}/order/create`, {
      method: 'POST',
      headers,
      signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
      cache: 'no-store',
    });
    const text = await backendRes.text();

    if (!backendRes.ok) {
      return jsonResponse(
        {
          message: text || 'Create order failed',
          backendStatus: backendRes.status,
        },
        backendRes.status,
      );
    }

    let data: any;
    try {
      data = JSON.parse(text);
    } catch {
      data = { text };
    }

    return jsonResponse(data);
  } catch (error) {
    console.error('API /order/create error:', error);

    if (error instanceof Error && error.name === 'TimeoutError') {
      return jsonResponse({ message: 'Create order request timed out' }, 504);
    }

    return jsonResponse({ message: 'Internal server error' }, 500);
  }
}
