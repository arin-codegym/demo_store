'use server';
import { fetchWithAuthServer } from '@/lib/fetchWithAuth.server';
import { FavoriteItem } from '@/utils/types';
import { revalidatePath } from 'next/cache';
import { headers } from 'next/headers';

export const fetchFavoriteId = async ({ productId }: { productId: string }) => {
  try {
    const headerStore = await headers();
    const cookie = headerStore.get('cookie') ?? '';

    const accessToken = cookie.match(/accessToken=([^;]+)/)?.[1] ?? '';
    const res = await fetch(
      `${process.env.API_EXTERNAL}/favorites/check/${productId}`,
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
        cache: 'no-store',
      },
    );

    if (!res.ok) return null;

    const data = await res.json();
    return data?.favoriteId === '' ? null : data?.favoriteId || null;
  } catch (error) {
    console.error('Lỗi fetchFavoriteId:', error);
    return null;
  }
};

export const toggleFavoriteAction = async (prevState: {
  productId: string;
  favoriteId: string | null;
  pathname: string;
}) => {
  const { productId, favoriteId, pathname } = prevState;

  try {
    let res: Response;
    if (favoriteId) {
      res = await fetch(
        `${process.env.API_EXTERNAL}/favorites/toggle/${productId}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          // body: JSON.stringify({
          //   productId,
          //   favoriteId,
          // }),
          cache: 'no-store',
        },
      );
    } else {
      res = await fetch(`${process.env.API_EXTERNAL}/favorites/toggle`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          productId,
          favoriteId,
        }),
        cache: 'no-store',
      });
    }

    if (!res.ok) {
      throw new Error('Toggle favorite failed');
    }

    revalidatePath(pathname);

    return {
      message: favoriteId ? 'Removed from Favorites' : 'Added to Favorites',
    };
  } catch (error) {
    console.error(error);
    return { message: 'Something went wrong' };
  }
};

export const fetchProductUserFavorites = async () => {
  const headerStore = await headers();
  const cookie = headerStore.get('cookie') ?? '';

  const accessToken = cookie.match(/accessToken=([^;]+)/)?.[1] ?? '';
  // Giá trị mặc định để dùng lại nhiều lần
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
    // Thay vì return [], hãy return defaultReturn
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
    // Thay vì return [], hãy return defaultReturn
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
