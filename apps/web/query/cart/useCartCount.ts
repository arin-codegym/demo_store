import { fetchJsonWithAuth } from '@/lib/fetchWithAuth.client';
import { useQuery } from '@tanstack/react-query';

export function useCartCount() {
  return useQuery({
    queryKey: ['cart-count'],
    queryFn: async () => {
      const data = await fetchJsonWithAuth<{ cartCount: number }>(
        '/api/cart/count',
      );
      return data?.cartCount ?? 0;
    },
    staleTime: 1000 * 60, // cache 1 phút
    retry: false, // chỉ tắt retry khi lỗi, không liên quan việc đổi tab
  });
}
