'use client';

import { ApiError, pickMessage, readBody } from './error';

/**
 * fetchWithAuth (client-only)
 *
 * Use this from Client Components and React Query hooks. Requests should go to
 * same-origin Next route handlers (`/api/...`) so the browser can store any
 * refreshed httpOnly cookies forwarded by those handlers.
 */

export type FetchWithAuthOptions = RequestInit & {
  /** Retry the original request once after a successful refresh. */
  autoRefresh?: boolean;

  /** Same-origin endpoint that refreshes cookies through the Next backend. */
  refreshEndpoint?: string;
};

const refreshRequests = new Map<string, Promise<void>>();

function refreshSession(refreshEndpoint: string): Promise<void> {
  // Look up the in-flight refresh Promise for this endpoint first.
  let refreshRequest = refreshRequests.get(refreshEndpoint);

  if (!refreshRequest) {
    // If none exists, this caller creates the real refresh HTTP request and
    // stores its Promise so later callers can reuse the same value.
    refreshRequest = fetch(refreshEndpoint, {
      method: 'POST',
      credentials: 'include',
    })
      .then((refreshRes) => {
        if (!refreshRes.ok) {
          throw new Error('UNAUTHORIZED');
        }
      })
      .finally(() => {
        refreshRequests.delete(refreshEndpoint);
      });

    refreshRequests.set(refreshEndpoint, refreshRequest);
  }

  // If the key already existed, no new fetch is created; this returns the
  // existing Promise so parallel 401 handlers all await one refresh request.
  return refreshRequest;
}

export async function fetchWithAuth<T = any>(
  url: string,
  options: FetchWithAuthOptions = {},
): Promise<Response> {
  const {
    autoRefresh = true,
    refreshEndpoint = '/api/auth/refresh',
    ...init
  } = options;

  const buildHeaders = () => {
    const finalHeaders = new Headers(init.headers);

    // Do not set Content-Type for FormData; the browser must add the boundary.
    if (!finalHeaders.has('Content-Type') && typeof init.body === 'string') {
      finalHeaders.set('Content-Type', 'application/json');
    }

    finalHeaders.set('Accept', 'application/json');

    return finalHeaders;
  };

  const doFetch = async (): Promise<Response> => {
    return fetch(url, {
      ...init,
      credentials: 'include',
      headers: buildHeaders(),
    });
  };

  let res = await doFetch();

  // A failed refresh means the caller should redirect or clear cached auth state.
  if (res.status === 401 && autoRefresh) {
    await refreshSession(refreshEndpoint);
    res = await doFetch();
  }

  // if (!res.ok) {
  //   const { data, message } = await readBody(res);
  //   throw new ApiError(
  //     message ?? `API error (${res.status})`,
  //     res.status,
  //     data,
  //   );
  // }

  // 204 No Content
  // if (res.status === 204) return null;

  // Nếu response không phải JSON (hiếm), trả text
  // const ct = res.headers.get('content-type') || '';
  // if (!ct.includes('application/json')) {
  //   const text = await res.text();
  //   return (text as unknown as T) ?? null;
  // }

  // return (await res.json()) as T;
  return res;
}

export async function fetchJsonWithAuth<T = unknown>(
  url: string,
  options: FetchWithAuthOptions = {},
): Promise<T | null> {
  const res = await fetchWithAuth(url, options);

  if (!res.ok) {
    const { data, message } = await readBody(res);
    throw new ApiError(
      message ?? `API error (${res.status})`,
      res.status,
      data,
    );
  }

  if (res.status === 204) return null;

  const ct = res.headers.get('content-type') || '';
  if (!ct.includes('application/json')) {
    const text = await res.text();
    return (text as T) ?? null;
  }

  return (await res.json()) as T;
}

export function postJsonWithAuth<T>(
  url: string,
  data: unknown,
  options: FetchWithAuthOptions = {},
) {
  return fetchJsonWithAuth<T>(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {}),
    },
    body: JSON.stringify(data),
    ...options,
  });
}

export function putJsonWithAuth<T>(
  url: string,
  data: unknown,
  options: FetchWithAuthOptions = {},
) {
  return fetchJsonWithAuth<T>(url, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {}),
    },
    body: JSON.stringify(data),
    ...options,
  });
}

export function deleteWithAuth(
  url: string,
  options: FetchWithAuthOptions = {},
) {
  return fetchWithAuth(url, {
    method: 'DELETE',
    ...options,
  });
}
