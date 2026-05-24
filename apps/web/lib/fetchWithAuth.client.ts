'use client';

import { ApiError, pickMessage, readBody } from './error';

/**
 * fetchWithAuth (client-only)
 * - Dùng trong React Query / Client Components.
 * - Luôn gửi cookie (credentials: "include").
 * - Nếu gặp 401 => gọi /api/auth/refresh (Next route handler) để refresh cookie,
 *   rồi retry request gốc 1 lần.
 *
 * Quy ước:
 * - url nên là endpoint cùng origin (ví dụ "/api/...") để tránh CORS.
 * - /api/auth/refresh phải là route.ts proxy sang Spring và forward Set-Cookie về browser.
 */

export type FetchWithAuthOptions = RequestInit & {
  /**
   * Nếu true (mặc định), tự gọi refresh khi gặp 401 và retry 1 lần.
   */
  autoRefresh?: boolean;

  /**
   * Endpoint refresh nội bộ (mặc định: "/api/auth/refresh")
   */
  refreshEndpoint?: string;
};
/* Đổi signature fetchWithAuth để overload theo “có thể null” / “không null” */
// export function fetchWithAuth<T = any>(url: string, options?: FetchWithAuthOptions & { allowNull: true }): Promise<T | null>;
// export function fetchWithAuth<T = any>(url: string, options?: FetchWithAuthOptions & { allowNull?: false }): Promise<T>;

/* Use : Nếu muốn check res.ok
const res = await fetchWithAuth(`/api/favorites/delete/${currentFavoriteId}`, {
  method: 'DELETE',
  cache: 'no-store',
});
if (!res.ok) {
  throw new Error('Failed to remove favorite');
}
*/
export async function fetchWithAuth<T = any>(
  url: string,
  options: FetchWithAuthOptions = {},
): Promise<Response> {
  const {
    autoRefresh = true,
    refreshEndpoint = '/api/auth/refresh',
    ...init
  } = options;

  // Merge headers an toàn (không override lung tung)
  const buildHeaders = () => {
    const finalHeaders = new Headers(init.headers);

    // Chỉ set Content-Type khi body là JSON string
    // (nếu bạn gửi FormData thì để browser tự set boundary)
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
  // Access token hết hạn / chưa có access nhưng có refresh
  if (res.status === 401 && autoRefresh) {
    const refreshRes = await fetch(refreshEndpoint, {
      method: 'POST',
      credentials: 'include',
    });
    if (!refreshRes.ok) {
      // refresh fail => caller tự xử lý (redirect login, clear cache, ...)
      throw new Error('UNAUTHORIZED');
    }

    // retry 1 lần sau khi refresh
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

/* Use : chỉ cần data
const data = await fetchJsonWithAuth<{ message: string }>(
  `/api/favorites/add/${productId}`,
  {
    method: 'POST',
    cache: 'no-store',
    body: JSON.stringify(productId),
  }
);
console.log(data?.message);
Với case favorite add:
await fetchJsonWithAuth<{ message: string }>(
  `/api/favorites/add/${productId}`,
  {
    method: 'POST',
    cache: 'no-store',
  }
);
*/

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
