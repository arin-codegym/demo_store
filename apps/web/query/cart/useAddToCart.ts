'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';

type AddToCartPayload = {
  productId: string;
  amount: number;
};

export function useAddToCart() {
  const queryClient = useQueryClient();

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

    onMutate: async (newItem) => {
      await queryClient.cancelQueries({ queryKey: ['cart'] });

      const previousCart = queryClient.getQueryData<any>(['cart']);
      const previousCount = queryClient.getQueryData(['cart-count']);
      queryClient.setQueryData(['cart'], (old: any) => {
        if (!old) return old;

        const existingItem = old.cartDetails.cartItems.find(
          (item: any) => item.productId === newItem.productId,
        );

        const cartItems = existingItem
          ? old.cartDetails.cartItems.map((item: any) =>
              item.productId === newItem.productId
                ? { ...item, amount: item.amount + newItem.amount }
                : item,
            )
          : [
              ...old.cartDetails.cartItems,
              {
                productId: newItem.productId,
                amount: newItem.amount,
              },
            ];

        return {
          ...old,
          cartDetails: {
            ...old.cartDetails,
            cartItems,
            numItemsInCart: old.cartDetails.numItemsInCart + newItem.amount,
          },
        };
      });
      queryClient.setQueryData(['cart-count'], (old: number) => {
        return (old || 0) + newItem.amount;
      });
      return { previousCart, previousCount };
    },

    onError: (err, variables, context) => {
      if (context?.previousCart) {
        queryClient.setQueryData(['cart'], context.previousCart);
      }
      queryClient.setQueryData(['cart-count'], context?.previousCount);
    },

    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart'] });
      queryClient.invalidateQueries({ queryKey: ['cart-count'] });
    },
  });
}
