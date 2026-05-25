import { fetchJsonWithAuth } from '@/lib/fetchWithAuth.client';
import { useQuery } from '@tanstack/react-query';

export function useCartCount(enabled = true) {
  return useQuery({
    queryKey: ['cart-count'],
    queryFn: async () => {
      const data = await fetchJsonWithAuth<{ cartCount: number }>(
        '/api/cart/count',
      );
      return data?.cartCount ?? 0;
    },
    enabled,
    staleTime: 1000 * 60,
    retry: false,
  });
}
