'use client';

// import { fetchWithAuth } from '@/lib/fetchWithAuth';
// import { useMutation, useQueryClient } from '@tanstack/react-query';

// export function useAddToCart() {
//   const queryClient = useQueryClient();

//   return useMutation({
//     mutationFn: async (payload: { productId: string; amount: number }) => {
//       // const res = await fetch('/api/cart/add', {
//       //   method: 'POST',
//       //   credentials: 'include',
//       //   headers: { 'Content-Type': 'application/json' },
//       //   body: JSON.stringify(payload),
//       // });

//       // if (!res.ok) throw new Error('Add cart failed');
//       // return res.json();
//       return fetchWithAuth('/api/cart/add', {
//         method: 'POST',
//         headers: { 'Content-Type': 'application/json' },
//         body: JSON.stringify(payload),
//       });
//     },

//     onSuccess: () => {
//       queryClient.invalidateQueries({ queryKey: ['cart-count'] });
//       queryClient.invalidateQueries({ queryKey: ['cart'] });
//     },
//   });
// }

import { useMutation, useQueryClient } from '@tanstack/react-query';

type AddToCartPayload = {
  productId: string;
  amount: number;
};

export function useAddToCart() {
  const queryClient = useQueryClient();
  /* onMutate → mutationFn → result → onSuccess/onError → onSettled */
  return useMutation({
    mutationFn: async ({ productId, amount }: AddToCartPayload) => {
      const res = await fetch('/api/cart/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ productId, amount }),
      });

      if (!res.ok) throw new Error('Add to cart failed');

      return res.json();
    },

    // 🚀 OPTIMISTIC UPDATE
    onMutate: async (newItem) => {
      await queryClient.cancelQueries({ queryKey: ['cart'] });

      const previousCart = queryClient.getQueryData<any>(['cart']);
      const previousCount = queryClient.getQueryData(['cart-count']);
      queryClient.setQueryData(['cart'], (old: any) => {
        if (!old) return old;

        const existingItem = old.cartDetails.cartItems.find(
          (item: any) => item.productId === newItem.productId,
        );

        if (existingItem) {
          existingItem.amount += newItem.amount;
        } else {
          old.cartDetails.cartItems.push({
            productId: newItem.productId,
            amount: newItem.amount,
          });
        }

        old.cartDetails.numItemsInCart += newItem.amount;

        return { ...old };
      });
      queryClient.setQueryData(['cart-count'], (old: number) => {
        return (old || 0) + newItem.amount;
      });
      return { previousCart, previousCount };
    },

    // ❌ rollback nếu fail
    onError: (err, variables, context) => {
      if (context?.previousCart) {
        queryClient.setQueryData(['cart'], context.previousCart);
      }
      queryClient.setQueryData(['cart-count'], context?.previousCount);
    },

    // ✅ sync lại data thật từ server
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart'] });
      queryClient.invalidateQueries({ queryKey: ['cart-count'] });
    },
  });
}
