// useUpdateUser.ts
import { fetchWithAuth } from '@/lib/fetchWithAuth.client';
import { useMutation } from '@tanstack/react-query';
import axios from 'axios';

export default function useUpdateUser() {
  return useMutation({
    mutationFn: async (vars: {
      userId: string | number;
      payload: { status: 'ACTIVE' | 'BANNED' | 'DELETED'; roles: string[] };
    }) => {
      const { userId, payload } = vars;
      const res = fetchWithAuth(`/api/admin/users/update/${userId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      return res;
    },
  });
}
/* ví dụ dùng mutate mà không dùng mutateAsync => rắc rối
const { mutate } = useUpdateUser();

const mutatePromise = (vars) =>
  new Promise((resolve, reject) => {
    mutate(vars, {
      onSuccess: resolve,
      onError: reject,
    });
  });

const saveAll = async (dirtyUsers) => {
  try {
    for (const u of dirtyUsers) {
      await mutatePromise({ userId: u.userId, payload: u.payload });
    }
    toast({ title: 'Saved all' });
  } catch (e) {
    toast({ title: 'Save all failed', variant: 'destructive' });
  }
};
*/
