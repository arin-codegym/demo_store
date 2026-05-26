'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';

type UpdatePayload = {
  cartItemId: string;
  amount: number;
  updatedAt?: string;
};

export function useUpdateCartItem() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ cartItemId, amount, updatedAt }: UpdatePayload) => {
      const res = await fetch(`/api/cart/update-item-cart`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ cartItemId, amount, updatedAt }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Update failed');
      return data;
    },

    onMutate: async ({ cartItemId, amount }) => {
      await queryClient.cancelQueries({ queryKey: ['cart'] });

      const previousCart = queryClient.getQueryData<any>(['cart']);
      queryClient.setQueryData(['cart'], (old: any) => {
        if (!old) return old;

        const updatedCartItems = old.cartDetails.cartItems.map((item: any) =>
          item.cartItemId === cartItemId
            ? { ...item, amount }
            : item,
        );
        const updatedNumItems = updatedCartItems.reduce(
          (sum: number, item: any) => sum + item.amount,
          0,
        );
        return {
          ...old,
          cartDetails: {
            ...old.cartDetails,
            cartItems: updatedCartItems,
            numItemsInCart: updatedNumItems,
          },
        };
      });

      return { previousCart };
    },

    onError: (err, vars, context) => {
      if (context?.previousCart !== undefined) {
        queryClient.setQueryData(['cart'], context.previousCart);
      }
      queryClient.invalidateQueries({ queryKey: ['cart'] });
    },

    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['cart'] });
    },
  });
}
