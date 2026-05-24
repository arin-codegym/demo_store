// 'use client';
// import axios from 'axios';
// import { useSearchParams } from 'next/navigation';
// import React, { useCallback } from 'react';
// import { loadStripe } from '@stripe/stripe-js';
// import {
//   EmbeddedCheckoutProvider,
//   EmbeddedCheckout,
// } from '@stripe/react-stripe-js';
import CheckoutClient from './CheckoutClient';

// const stripePromise = loadStripe(
//   process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY as string,
// );

type CheckoutPageProps = {
  searchParams: Promise<{
    orderId?: string;
    cartId?: string;
  }>;
};
export default async function CheckoutPage({
  searchParams,
}: CheckoutPageProps) {
  /* nếu dùng useSearchParams thì phải bọc trong 
    <Suspense fallback={<div>Loading...</div>}>
      <CheckoutClient />
    </Suspense>
    Cách 1 là nhận searchParams từ next tức server nó tự gửi
    còn useSearchParams là đọc từ browser và hook này đặc biệt nên phải bọc
  */
  // const searchParams = useSearchParams(); 
  const sp = await searchParams;
  // const orderId = searchParams.get('orderId');
  // const cartId = searchParams.get('cartId');
  const orderId = sp.orderId ?? null;
  const cartId = sp.cartId ?? null;
 /* Tách thành client component */
  //   const fetchClientSecret = useCallback(async () => {
  //   const res = await fetch('/api/payment', {
  //     method: 'POST',
  //     headers: { 'Content-Type': 'application/json' },
  //     body: JSON.stringify({ orderId, cartId }),
  //   });

  //   if (!res.ok) throw new Error('Failed');

  //   const data = await res.json();
  //   return data.clientSecret;
  // }, [orderId, cartId]);
  // const fetchClientSecret = useCallback(async () => {
  //   // Create a Checkout Session
  //   const response = await axios.post(
  //     '/api/payment/create-session',
  //     {
  //       orderId: orderId,
  //       cartId: cartId,
  //     },
  //     { withCredentials: true },
  //   );
  //   return response.data.clientSecret;
  // }, [orderId, cartId]);

  // const options = { fetchClientSecret };

  return (
    // <div id='checkout'>
    //   <EmbeddedCheckoutProvider stripe={stripePromise} options={options}>
    //     <EmbeddedCheckout />
    //   </EmbeddedCheckoutProvider>
    // </div>
    <CheckoutClient orderId={orderId} cartId={cartId} />
  );
}
