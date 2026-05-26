'use server';
import { fetchWithAuth } from '@/lib/fetchWithAuth.client';

import { cookies } from 'next/headers';
// import renderError from '@/utils/actions';
const BACKEND_URL = process.env.API_EXTERNAL;
export async function getCartCount() {
  const res = await fetchWithAuth('/cart/count');
  if (!res.ok) return 0;
  const data = await res.json();
  return data.count;
}

export async function fetchCartDetails() {
  try {
    const cookieStore = await cookies();

    const res = await fetch(`${BACKEND_URL}/cart/details`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        Cookie: cookieStore.toString(), // forward cookie auth
      },
      cache: 'no-store',
    });

    if (res.status === 401) {
      return null;
    }

    if (!res.ok) {
      throw new Error('Failed to fetch cart');
    }

    const data = await res.json();

    return data;
  } catch (err) {
    console.error('fetchCartDetails error:', err);
    return null;
  }
}

export const removeCartItemAction = async (cartItemId: any) => {
  const cookieStore = await cookies();
  const res = await fetch(`${BACKEND_URL}/cart/remove/${cartItemId}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      Cookie: cookieStore.toString(), // forward cookie auth
    },
    cache: 'no-store',
  });
  if (!res.ok) {
    throw new Error('Failed to remove item');
  }
  return { message: 'Item removed from cart' };
};
