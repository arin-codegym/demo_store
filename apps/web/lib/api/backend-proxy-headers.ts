import { NextRequest } from 'next/server';

type Options = {
  json?: boolean;
};

export function buildBackendProxyHeaders(req: NextRequest, options: Options = {}) {
  const headers = new Headers();

  const cookie = req.headers.get('cookie');
  if (cookie) headers.set('cookie', cookie);

  const authorization = req.headers.get('authorization');
  if (authorization) headers.set('authorization', authorization);

  const accept = req.headers.get('accept');
  headers.set('accept', accept ?? 'application/json');

  const contentType = req.headers.get('content-type');
  if (contentType) {
    headers.set('content-type', contentType);
  } else if (options.json) {
    headers.set('content-type', 'application/json');
  }

  return headers;
}

export function toProxyResponse(upstream: Response) {
  const headers = new Headers();
  const contentType = upstream.headers.get('content-type');
  if (contentType) headers.set('content-type', contentType);

  return new Response(upstream.body, {
    status: upstream.status,
    statusText: upstream.statusText,
    headers,
  });
}
