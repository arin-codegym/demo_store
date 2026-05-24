'use client';

import { useActionState, useEffect } from 'react';
import { deleteProductAction } from '@/action/product-action';
import { toast } from '../ui/use-toast';

const initialState = { message: '', errors: {} };

export default function DeleteProduct({
  productId,
  image,
}: {
  productId: string;
  image: string;
}) {
  const [state, formAction, pending] = useActionState(
    deleteProductAction,
    initialState,
  );

  useEffect(() => {
    if (state.message) {
      toast({ description: state.message });
    }
  }, [state]);

  return (
    <form action={formAction}>
      <input type='hidden' name='productId' value={productId} />
      <input type='hidden' name='image' value={image} />
      <button type='submit' disabled={pending}>
        Delete
      </button>
    </form>
  );
}
