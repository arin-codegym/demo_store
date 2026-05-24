import { useQuery } from '@tanstack/react-query';

// Tuỳ bạn định nghĩa Order type ở đâu
import { fetchJsonWithAuth, fetchWithAuth } from '@/lib/fetchWithAuth.client';
import { Order } from '@/utils/types';

export function useAdminOrders() {
  return useQuery<Order[]>({
    queryKey: ['adminOrders'],
    queryFn: async () => {
      const data = await fetchJsonWithAuth<Order[]>('/api/admin/orders');
      return data ?? []; // ✅ không bao giờ null
    },
    retry: false, // tránh retry spam khi 401/403
  });
}
