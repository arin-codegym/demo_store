import 'server-only';

/**
 * Server-side fetch wrapper for route handlers and server actions.
 *
 * It forwards the incoming Cookie header, refreshes once on 401, retries the
 * original request with the refreshed cookies, and returns Set-Cookie values so
 * the caller can forward them to the browser.
 */

export type FetchWithAuthServerResult = {
  response: Response;
  setCookie: string[];
};

function splitSetCookie(raw: string | null): string[] {
  if (!raw) return [];

  // A single Set-Cookie header can contain commas in Expires, so split only at
  // the start of the next cookie pair.
  return raw
    .split(/,(?=\s*[^;]+=[^;]+)/g)
    .map((s) => s.trim())
    .filter(Boolean);
}

function mergeCookieHeader(
  originalCookieHeader: string | null | undefined,
  setCookies: string[],
): string {
  const cookieMap = new Map<string, string>();

  if (originalCookieHeader) {
    originalCookieHeader
      .split(';')
      .map((part) => part.trim())
      .filter(Boolean)
      .forEach((pair) => {
        const idx = pair.indexOf('=');
        if (idx > 0) {
          const name = pair.slice(0, idx).trim();
          const value = pair.slice(idx + 1).trim();
          cookieMap.set(name, value);
        }
      });
  }

  for (const setCookie of setCookies) {
    const firstPart = setCookie.split(';')[0]?.trim();
    if (!firstPart) continue;

    const idx = firstPart.indexOf('=');
    if (idx > 0) {
      const name = firstPart.slice(0, idx).trim();
      const value = firstPart.slice(idx + 1).trim();
      cookieMap.set(name, value);
    }
  }

  return Array.from(cookieMap.entries())
    .map(([name, value]) => `${name}=${value}`)
    .join('; ');
}

export async function fetchWithAuthServer(
  url: string,
  options: RequestInit & {
    cookieHeader?: string | null;
    refreshUrl?: string;
  } = {},
): Promise<FetchWithAuthServerResult> {
  const {
    cookieHeader,
    refreshUrl = `${process.env.API_EXTERNAL}/auth/refresh`,
    ...fetchOptions
  } = options;

  const buildHeaders = (cookie?: string | null) => {
    const headers = new Headers(fetchOptions.headers);

    if (!headers.has('Content-Type') && typeof fetchOptions.body === 'string') {
      headers.set('Content-Type', 'application/json');
    }

    if (!headers.has('Accept')) {
      headers.set('Accept', 'application/json');
    }

    if (cookie) {
      headers.set('cookie', cookie);
    }

    return headers;
  };

  const doFetch = async (cookie?: string | null) => {
    const response = await fetch(url, {
      ...fetchOptions,
      headers: buildHeaders(cookie),
    });

    const setCookie = splitSetCookie(response.headers.get('set-cookie'));

    return { response, setCookie };
  };

  let currentCookieHeader = cookieHeader ?? null;

  let { response, setCookie } = await doFetch(currentCookieHeader);

  if (response.status === 401) {
    const refreshRes = await fetch(refreshUrl, {
      method: 'POST',
      headers: cookieHeader ? { cookie: cookieHeader } : {},
    });

    const refreshSetCookie = splitSetCookie(
      refreshRes.headers.get('set-cookie'),
    );

    setCookie = [...setCookie, ...refreshSetCookie];

    if (!refreshRes.ok) {
      return {
        response: refreshRes,
        setCookie,
      };
    }

    // Retry with the freshly issued cookie values instead of the stale request
    // header, otherwise the retry would repeat the same 401.
    currentCookieHeader = mergeCookieHeader(cookieHeader, refreshSetCookie);

    ({ response } = await doFetch(currentCookieHeader));
  }

  return {
    response,
    setCookie,
  };
}
