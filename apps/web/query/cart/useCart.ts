import { fetchWithAuth } from '@/lib/fetchWithAuth.client';
import { useQuery } from '@tanstack/react-query';

export function useCart() {
  return useQuery({
    queryKey: ['cart'],
    queryFn: async () => {
      // Fetch through the Next route handler so cookie refresh stays same-origin.
      const res = await fetchWithAuth('/api/cart/details', {
        credentials: 'include',
      });
      if (!res.ok) return null;
      const data = res.json();
      return data;
    },
    staleTime: 1000 * 30,
  });
}
