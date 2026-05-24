// /query/admin/useCreateUser.ts
import { ApiPayload } from '@/app/(dashboard)/admin/users/CreateUserForm';
import { fetchWithAuth, postJsonWithAuth } from '@/lib/fetchWithAuth.client';
import { useMutation, useQueryClient } from '@tanstack/react-query';

export default function useCreateUser() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: async (payload: ApiPayload) => {
      // const res = await fetchWithAuth('/api/admin/users/create', {
      //   method: 'POST',
      //   body: JSON.stringify(payload),
      // });

      // if (!res.ok) throw Error('Failed create user');
      // const data = await res.json();

      // return data;
      return postJsonWithAuth('/api/admin/users/create', payload);
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['adminUsers'] });
    },
  });
}
