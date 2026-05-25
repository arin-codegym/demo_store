import { fetchWithAuth } from '@/lib/fetchWithAuth.client';
import { useQuery } from '@tanstack/react-query';

export default function useGetUsers() {
  return useQuery<any[]>({
    queryKey: ['adminUsers'],
    queryFn: async () => {
      const res = await fetchWithAuth('/api/admin/users');
      const data = await res.json();

      return data ?? [];
    },
    retry: false,
  });
}
