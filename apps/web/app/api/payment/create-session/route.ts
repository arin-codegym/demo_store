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

    const body = await req.json();
    const { orderId, cartId } = body;

    if (!orderId || !cartId) {
      return jsonResponse({ message: 'Missing orderId or cartId' }, 400);
    }

    const response = await fetch(`${BACKEND_URL}/payment/create-session`, {
      method: 'POST',
      headers: buildBackendProxyHeaders(req, { json: true }),
      body: JSON.stringify({ orderId, cartId }),
      signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
      cache: 'no-store',
    });

    const text = await response.text();
    let data: any = {};

    if (text) {
      try {
        data = JSON.parse(text);
      } catch {
        data = { message: text };
      }
    }

    if (!response.ok) {
      return jsonResponse(
        {
          message:
            data.message ||
            data.error ||
            'Backend failed to create payment session',
          backendStatus: response.status,
        },
        response.status,
      );
    }

    if (!data.clientSecret) {
      return jsonResponse(
        { message: 'Backend did not return Stripe client secret' },
        502,
      );
    }

    return jsonResponse(data);
  } catch (error) {
    console.error('API /payment/create-session error:', error);

    if (error instanceof Error && error.name === 'TimeoutError') {
      return jsonResponse({ message: 'Payment backend request timed out' }, 504);
    }

    return jsonResponse({ message: 'Error creating payment session' }, 500);
  }
}
