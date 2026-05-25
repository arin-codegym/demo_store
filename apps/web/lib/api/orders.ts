import { cookies } from 'next/headers';

export async function fetchOrdersServer() {
  try {
    const cookieStore = await cookies();
    const cookieHeader = cookieStore.toString();

    // This runs during server prefetch, so use native fetch and forward the
    // browser cookies explicitly instead of importing the client fetch wrapper.
    const res = await fetch(
      `${process.env.API_EXTERNAL}/orders/is-paid`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          Cookie: cookieHeader || '',
        },
        cache: 'no-store',
      },
    );
    if (res.status === 401) {
      return null;
    }
    if (!res.ok) {
      throw new Error('Failed to fetch orders');
    }

    const json = await res.json();
    return json.orders ?? [];
  } catch (err) {
    console.error('fetchOrdersServer error:', err);
    return null;
  }
}
