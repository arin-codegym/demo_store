'use client';
import { FavoriteItem, Product } from '@/utils/types';
import { useEffect, useMemo, useState } from 'react';
import ProductsGrid from '../products/ProductsGrid';
import { fetchWithAuth } from '@/lib/fetchWithAuth.client';
import { useCurrentUser } from '@/query/auth/useCurrentUser';

export const FeaturedProductChild = ({
  initialProducts,
}: {
  initialProducts: Product[];
}) => {
  const [products, setProducts] = useState<Product[]>(initialProducts);
  const [favorites, setFavorites] = useState<FavoriteItem[]>([]);
  const { data: user } = useCurrentUser();
  function handleFavoriteChanged(
    productId: string,
    nextFavoriteId: string | null,
  ) {
    setFavorites((prev) => {
      if (!nextFavoriteId) {
        return prev.filter((item) => item.productId !== productId);
      }

      const exists = prev.some((item) => item.productId === productId);

      if (exists) {
        return prev.map((item) =>
          item.productId === productId
            ? { ...item, favoriteId: nextFavoriteId }
            : item,
        );
      }

      return [...prev, { productId, favoriteId: nextFavoriteId }];
    });
  }
  useEffect(() => {
    async function loadFavorites() {
      try {
        if (user) {
          const res = await fetchWithAuth('/api/favorites', {
            method: 'GET',
            cache: 'no-store',
          });
          if (!res.ok) throw new Error('Failed to fetch favorites');
          const data: { favorites: FavoriteItem[] } = await res.json();
          setFavorites(data.favorites ?? []);
        } else {
          setFavorites([]);
        }
      } catch {
        setFavorites([]);
      }
    }

    loadFavorites();
  }, [user]);
  const favoriteMap = useMemo(() => {
    const map = new Map<string, string>();
    for (const item of favorites) {
      map.set(item.productId, item.favoriteId);
    }
    return map;
  }, [favorites]);
  return (
    <ProductsGrid
      products={products}
      favoriteMap={favoriteMap}
      onFavoriteChanged={handleFavoriteChanged}
    />
  );
};
