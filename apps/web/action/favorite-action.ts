'use server';
import { fetchWithAuthServer } from '@/lib/fetchWithAuth.server';
import { FavoriteItem } from '@/utils/types';
import { headers } from 'next/headers';

export const fetchProductUserFavorites = async () => {
  const headerStore = await headers();
  const cookie = headerStore.get('cookie') ?? '';

  const accessToken = cookie.match(/accessToken=([^;]+)/)?.[1] ?? '';
  const defaultReturn = {
    products: [],
    favoriteMap: new Map<string, string>(),
  };
  try {
    const result1 = await fetchWithAuthServer(
      `${process.env.API_EXTERNAL}/product/fetchProductUserFavorites`,
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        cache: 'no-store',
      },
    );

    // Keep the page render stable if the user is anonymous or the backend
    // rejects the request.
    if (!result1.response.ok) return defaultReturn;
    const products = await result1.response.json();

    const response2 = await fetchWithAuthServer(
      `${process.env.API_EXTERNAL}/favorites/fetchUserFavorites`,
      {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
          accept: 'application/json',
        },
        cache: 'no-store',
      },
    );

    if (!response2.response.ok) return { products, favoriteMap: new Map() };
    const { userFavorites: data }: { userFavorites: FavoriteItem[] } =
      await response2.response.json();
    // const favoriteMap = (() => {
    //   const map = new Map<string, string>();
    //   for (const item of data.favorites) {
    //     map.set(item.productId, item.favoriteId);
    //   }
    //   return map;
    // })();
    const favoriteMap = new Map<string, string>(
      data.map((item) => [item.productId, item.favoriteId]),
    );
    return { products, favoriteMap };
  } catch (error) {
    console.error('Lỗi fetchUserFavorites:', error);
    return { products: [], favoriteMap: new Map<string, string>() };
  }
};
