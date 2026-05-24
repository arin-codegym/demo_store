import 'server-only';

/**
 * Server-side fetch wrapper có:
 * - forward Cookie header
 * - nếu gặp 401 thì gọi refresh endpoint rồi retry 1 lần
 * - trả về Response thật + danh sách Set-Cookie để route handler forward về browser
 *
 * Quy ước:
 * - Hàm này KHÔNG parse JSON sẵn
 * - Hàm này KHÔNG trả object giả Response
 * - Caller tự quyết định:
 *   - return response trực tiếp
 *   - hoặc đọc response.json() rồi custom NextResponse
 */

export type FetchWithAuthServerResult = {
  response: Response;
  setCookie: string[];
};

function splitSetCookie(raw: string | null): string[] {
  if (!raw) return [];

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
/* retry ngay trong server 
Nó có thể tự gắn lại cookie khi được gọi trong action hoặc route 
tức bản chất phải được goị ở client và trong route phải thêm như ví dụ:
const { response, setCookie } = await fetchWithAuthServer(...);

const data = await response.json();
const res = NextResponse.json(data, { status: response.status });

for (const cookie of setCookie) {
  res.headers.append('set-cookie', cookie);
}

return res;

*/
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

  // 1) request chính
  let { response, setCookie } = await doFetch(currentCookieHeader);

  // 2) nếu 401 thì refresh rồi retry 1 lần
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

    // merge cookie mới từ refresh vào cookie header để retry chuẩn hơn
    currentCookieHeader = mergeCookieHeader(cookieHeader, refreshSetCookie);

    ({ response } = await doFetch(currentCookieHeader));
  }

  return {
    response,
    setCookie,
  };
}
