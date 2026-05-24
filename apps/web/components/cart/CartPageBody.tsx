'use client';

import { useCart } from '@/query/cart/useCart';
import SectionTitle from '../global/SectionTitle';
import CartItemsList from './CartItemsList';
import CartTotals from './CartTotals';
import { useCurrentUser } from '@/query/auth/useCurrentUser';
import { QueryClient } from '@tanstack/react-query';
import { useState } from 'react';

function CartPageBody() {
  const { data: cartData, isLoading: cartLoading } = useCart();
  const { data: user, isLoading: userLoading } = useCurrentUser();

  const isAuthenticated = !!user;

  if (userLoading || cartLoading) {
    return <div className='text-center py-12'>Loading cart...</div>;
  }
  if (!isAuthenticated) {
    return <SectionTitle text='Please sign in to view your cart' />;
  }

  if (!cartData?.cartDetails) {
    return <SectionTitle text='Empty cart' />;
  }

  return (
    <>
      <SectionTitle text='Shopping Cart' />
      <div className='mt-8 grid gap-4 lg:grid-cols-12'>
        <div className='lg:col-span-8'>
          <CartItemsList cartItems={cartData.cartDetails.cartItems} />
        </div>
        <div className='lg:col-span-4 lg:pl-4'>
          <CartTotals cart={cartData.cartDetails} />
        </div>
      </div>
    </>
  );
}

export default CartPageBody;
