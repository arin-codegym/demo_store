'use client';

import { HydrationBoundary } from '@tanstack/react-query';
import CartPageBody from './CartPageBody';

export default function CartClient({ dehydratedState }: any) {
  return (
    <HydrationBoundary state={dehydratedState}>
      <CartPageBody />
    </HydrationBoundary>
  );
}
