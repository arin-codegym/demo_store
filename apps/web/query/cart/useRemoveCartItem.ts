'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { removeCartItemAction } from '@/action/cart-action';
import { toast } from '@/components/ui/use-toast';

export function useRemoveCartItem() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: async (itemId: string) => {
      return await removeCartItemAction(itemId);
    },
    onMutate: async (itemId) => {
      await qc.cancelQueries({ queryKey: ['cart'] });

      const previousCart = qc.getQueryData(['cart']);

      qc.setQueryData(['cart'], (old: any) => ({
        ...old,
        cartItems: old.cartItems.filter((i: any) => i.id !== itemId),
      }));

      return { previousCart };
    },

    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['cart'] });
      qc.invalidateQueries({ queryKey: ['cart-count'] });

      toast({
        title: 'Item removed',
      });
    },

    onError: (err, variable, context) => {
      qc.setQueryData(['cart'], context?.previousCart);
      toast({
        title: 'Failed to remove item',
        variant: 'destructive',
      });
    },
  });
}
