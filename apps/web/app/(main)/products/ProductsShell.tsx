'use client';

import { useEffect, useMemo, useState, useTransition } from 'react';
import { searchProducts } from '@/action/product-action';
import ProductsGrid from '@/components/products/ProductsGrid';
import ProductsList from '@/components/products/ProductsList';
import { FavoriteItem, Product } from '@/utils/types';
import { Button } from '@/components/ui/button';
import { LuLayoutGrid, LuList } from 'react-icons/lu';
import { fetchWithAuth } from '@/lib/fetchWithAuth.client';
import { useCurrentUser } from '@/query/auth/useCurrentUser';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';

export default function ProductsShell({
  initialProducts,
}: {
  initialProducts: Product[];
}) {
  const [products, setProducts] = useState<Product[]>(initialProducts);
  const [layout, setLayout] = useState<'grid' | 'list'>('grid');
  const [favorites, setFavorites] = useState<FavoriteItem[]>([]);
  // const [search, setSearch] = useState('');
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();

  const search = searchParams.get('search') ?? '';
  const [isPending, startTransition] = useTransition();
  const { data: user } = useCurrentUser();

  const favoriteMap = useMemo(() => {
    const map = new Map<string, string>();
    for (const item of favorites) {
      map.set(item.productId, item.favoriteId);
    }
    return map;
  }, [favorites]);

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

  useEffect(() => {
    let cancelled = false;
    async function run() {
      if (!search.trim()) {
        startTransition(() => {
          setProducts(initialProducts);
        });
        return;
      }
      const result = await searchProducts(search);

      if (cancelled) return;
      // chỉ setProducts(...) mới được đánh dấu là transition
      startTransition(() => {
        setProducts(result);
      });
    }
    run();
    return () => {
      cancelled = true;
    };
    // startTransition(async () => {
    //   if (!search.trim()) {
    //     setProducts(initialProducts);
    //     return;
    //   }

    //   const result = await searchProducts(search);
    //   setProducts(result);
    // });
  }, [search, initialProducts]);
  /* search từ nguồn là url */
  // useEffect(() => {
  //   async function runSearch() {
  //     if (!search.trim()) {
  //       setProducts(initialProducts);
  //       return;
  //     }

  //     const result = await searchProducts(search);
  //     setProducts(result);
  //   }

  //   runSearch();
  // }, [search, initialProducts]);

  // async function handleSearch(value: string) {
  //   setSearch(value);

  //   startTransition(async () => {
  //     if (!value.trim()) {
  //       setProducts(initialProducts);
  //       return;
  //     }

  //     const result = await searchProducts(value);
  //     setProducts(result);
  //   });
  // }
  // function handleSearch(value: string) {
  //   const params = new URLSearchParams(searchParams.toString());

  //   if (value.trim()) {
  //     params.set('search', value);
  //   } else {
  //     params.delete('search');
  //   }

  //   startTransition(() => {
  //     const queryString = params.toString();
  //     router.replace(queryString ? `${pathname}?${queryString}` : pathname);
  //   });
  // }
  const totalProducts = products.length;
  return (
    <>
      <section className='mb-6 flex items-center justify-end gap-4'>
        {/* <input
          value={search}
          onChange={(e) => handleSearch(e.target.value)}
          placeholder='Search products...'
          className='border rounded px-3 py-2 w-full max-w-md'
        /> */}

        <div className='flex gap-x-4'>
          <Button
            variant={layout === 'grid' ? 'default' : 'ghost'}
            size='icon'
            onClick={() => setLayout('grid')}
          >
            <LuLayoutGrid />
          </Button>
          <Button
            variant={layout === 'list' ? 'default' : 'ghost'}
            size='icon'
            onClick={() => setLayout('list')}
          >
            <LuList />
          </Button>
        </div>
      </section>

      <section>
        <h4 className='font-medium text-lg mb-4'>
          {isPending
            ? 'Loading...'
            : `${totalProducts} product${totalProducts > 1 ? 's' : ''}`}
        </h4>

        {totalProducts === 0 ? (
          <h5 className='text-2xl mt-16'>
            Sorry, no products matched your search...
          </h5>
        ) : layout === 'grid' ? (
          <ProductsGrid
            products={products}
            favoriteMap={favoriteMap}
            onFavoriteChanged={handleFavoriteChanged}
          />
        ) : (
          <ProductsList
            products={products}
            favoriteMap={favoriteMap}
            onFavoriteChanged={handleFavoriteChanged}
          />
        )}
      </section>
    </>
  );
}
