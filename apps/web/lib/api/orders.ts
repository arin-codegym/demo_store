import { cookies } from 'next/headers';
import { fetchWithAuth } from '../fetchWithAuth.client';

export async function fetchOrdersServer() {
  try {
    const cookieStore = await cookies();
    const cookieHeader = cookieStore.toString();
    const res = await fetchWithAuth(
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
    return json.orders ?? []; // orders là key object nếu back-end trả về array mà không phải object
    // const text = await res.text();
    // console.log('RAW RESPONSE:', text);

    // const data = JSON.parse(text);
    // return data;
  } catch (err) {
    console.error('fetchCartDetails error:', err);
    return null;
  }
}
