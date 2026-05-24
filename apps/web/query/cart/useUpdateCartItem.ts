'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';

type UpdatePayload = {
  cartItemId: string;
  amount: number;
};

export function useUpdateCartItem() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ cartItemId, amount }: UpdatePayload) => {
      const res = await fetch(`/api/cart/update-item-cart`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ cartItemId, amount }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Update failed');
      return data;
    },

    // 🟢 optimistic update
    onMutate: async ({ cartItemId, amount }) => {
      await queryClient.cancelQueries({ queryKey: ['cart'] });

      const previousCart = queryClient.getQueryData<any>(['cart']);
      // const previousCart = queryClient.getQueryData(['cart']);
      queryClient.setQueryData(['cart'], (old: any) => {
        //   if (!old) return old;

        //   const item = old.cartDetails.cartItems.find(
        //     (i: any) => i.cartItemId === cartItemId,
        //   );

        //   if (item) {
        //     item.amount = amount; //❌ Sai (mutate trực tiếp)
        //   }

        //   old.cartDetails.numItemsInCart = old.cartDetails.cartItems.reduce(
        //     (sum: number, i: any) => sum + i.amount,
        //     0,
        //   );

        //   return { ...old };
        const updatedCartItems = old.cartDetails.cartItems.map((item: any) =>
          item.cartItemId === cartItemId
            ? { ...item, amount } // ✅ tạo object mới
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
    },

    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['cart'] });
      // queryClient.setQueryData(['cart'], data);
    },
  });
}
