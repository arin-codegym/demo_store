'use client';

import { useCallback } from 'react';
import axios from 'axios';
import {
  EmbeddedCheckout,
  EmbeddedCheckoutProvider,
} from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';

type Props = {
  orderId: string | null;
  cartId: string | null;
};
const stripePromise = loadStripe(
  process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY as string,
);

export default function CheckoutClient({ orderId, cartId }: Props) {
  const fetchClientSecret = useCallback(async () => {
    const response = await axios.post(
      '/api/payment/create-session',
      {
        orderId,
        cartId,
      },
      { withCredentials: true },
    );

    return response.data.clientSecret;
  }, [orderId, cartId]);

  const options = { fetchClientSecret };

  return (
    <div id='checkout'>
      <EmbeddedCheckoutProvider stripe={stripePromise} options={options}>
        <EmbeddedCheckout />
      </EmbeddedCheckoutProvider>
    </div>
  );
}
