// 'use client';
/* component chạy ở browser
↓
React mount
↓
useQuery chạy
↓
queryFn chạy → fetchWithAuth chạy.*/

// client hydrate

// => đôi khi chạy ở server.
/* |               | useAddToCart | useCart            |
| ------------- | ------------ | ------------------ |
| loại          | mutation     | query              |
| chạy khi nào  | user click   | component mount    |
| chạy ở đâu    | browser      | server hoặc client |
| phụ thuộc SSR | không        | có                 |
| dính lỗi env  | hiếm         | rất dễ             |
 */

import { fetchWithAuth } from '@/lib/fetchWithAuth.client';
import { useQuery } from '@tanstack/react-query';

export function useCart() {
  return useQuery({
    queryKey: ['cart'],
    queryFn: async () => {
      /* CartPage mount
          ↓
        React Query gọi queryFn ngay
          ↓
        queryFn chạy lúc SSR phase
          ↓
        fetchWithAuth chạy trong Node nên nó không hiểu fetchWithAuth ->crash */
      const res = await fetchWithAuth('/api/cart/details', {
        // method: 'GET',
        // headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
      });
      if (!res.ok) return null;
      const data = res.json();
      return data;
    },
    staleTime: 1000 * 30,
  });
}
