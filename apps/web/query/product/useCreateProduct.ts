import { fetchWithAuth } from '@/lib/fetchWithAuth.client';
import { useMutation, useQueryClient } from '@tanstack/react-query';

type CreateProduct = {
  name: string;
  company: string;
  price: number;
  description: string;
  featured: boolean;
  image?: string;
};

export default function useCreateUser() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: async (payload: CreateProduct) => {
      const res = await fetchWithAuth('/api/admin/product/create', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      return res;
    },
    onSuccess: () => {
      //   qc.invalidateQueries({ queryKey: ['adminUsers'] });
    },
    onError: async (_error, payload) => {
      if (payload.image) {
        await fetch('/api/upload-image', {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ imageUrl: payload.image }),
        });
      }
    },
  });
}
