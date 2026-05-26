import 'server-only';
import { NextRequest, NextResponse } from 'next/server';
import { fetchWithAuthServer } from '@/lib/fetchWithAuth.server';

type ProxyJsonOptions = {
  req: NextRequest;
  endpoint: string;
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  body?: unknown;
};

export async function proxyJson({
  req,
  endpoint,
  method = 'GET',
  body,
}: ProxyJsonOptions) {
  try {
    const cookieHeader = req.headers.get('cookie');

    // Centralize backend proxying so every route gets the same refresh/retry
    // behavior and cookie forwarding.
    const { response, setCookie } = await fetchWithAuthServer(
      `${process.env.API_EXTERNAL}${endpoint}`,
      {
        method,
        cookieHeader,
        headers: {
          Accept: 'application/json',
          ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
        },
        ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
      },
    );

    let res: NextResponse;

    if (response.status === 204) {
      res = NextResponse.json({ success: true }, { status: 200 });
    } else {
      const ct = response.headers.get('content-type') ?? '';

      if (ct.includes('application/json')) {
        const data = await response.json().catch(() => null);
        res = NextResponse.json(data, { status: response.status });
      } else {
        const text = await response.text().catch(() => '');
        res = NextResponse.json(
          { message: text || 'Request failed' },
          { status: response.status },
        );
      }
    }

    // Forward refreshed cookies from Spring back through the Next route handler.
    for (const cookie of setCookie) {
      res.headers.append('set-cookie', cookie);
    }

    return res;
  } catch (error) {
    return NextResponse.json(
      {
        message:
          error instanceof Error ? error.message : 'Internal server error',
      },
      { status: 500 },
    );
  }
}

export async function proxyPostJson(req: NextRequest, endpoint: string) {
  const payload = await req.json();
  return proxyJson({
    req,
    endpoint,
    method: 'POST',
    body: payload,
  });
}

export async function proxyGetJson(req: NextRequest, endpoint: string) {
  return proxyJson({
    req,
    endpoint,
    method: 'GET',
  });
}

export async function proxyPatchJson(req: NextRequest, endpoint: string) {
  const payload = await req.json();
  return proxyJson({
    req,
    endpoint,
    method: 'PATCH',
    body: payload,
  });
}
