import { useQuery } from '@tanstack/react-query';

import { fetchJsonWithAuth } from '@/lib/fetchWithAuth.client';
import { Order } from '@/utils/types';

export function useAdminOrders() {
  return useQuery<Order[]>({
    queryKey: ['adminOrders'],
    queryFn: async () => {
      const data = await fetchJsonWithAuth<Order[]>('/api/admin/orders');
      return data ?? [];
    },
    retry: false,
  });
}
