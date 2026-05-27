'use client';

import { useEffect, useMemo, useState } from 'react';
import {
  EmbeddedCheckout,
  EmbeddedCheckoutProvider,
} from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';

type Props = {
  orderId: string | null;
  cartId: string | null;
};

const stripePublishableKey = process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY;
const stripePromise = stripePublishableKey
  ? loadStripe(stripePublishableKey)
  : null;

export default function CheckoutClient({ orderId, cartId }: Props) {
  const [clientSecret, setClientSecret] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!stripePublishableKey) {
      setError('Checkout is not configured. Missing Stripe public key.');
      return;
    }

    if (!orderId || !cartId) {
      setError('Missing order information. Please create the order again.');
      return;
    }

    const controller = new AbortController();
    const timeout = window.setTimeout(() => controller.abort(), 15000);

    async function createPaymentSession() {
      try {
        setError(null);
        setClientSecret(null);

        const response = await fetch('/api/payment/create-session', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ orderId, cartId }),
          credentials: 'include',
          signal: controller.signal,
        });

        const text = await response.text();
        const data = text ? JSON.parse(text) : {};

        if (!response.ok) {
          throw new Error(
            data.message || data.error || 'Cannot start checkout session.',
          );
        }

        if (!data.clientSecret) {
          throw new Error('Checkout session did not return a client secret.');
        }

        setClientSecret(data.clientSecret);
      } catch (err) {
        if (controller.signal.aborted) {
          setError('Checkout request timed out. Please try again.');
          return;
        }

        setError(
          err instanceof Error
            ? err.message
            : 'Cannot start checkout session.',
        );
      } finally {
        window.clearTimeout(timeout);
      }
    }

    void createPaymentSession();

    return () => {
      controller.abort();
      window.clearTimeout(timeout);
    };
  }, [orderId, cartId]);

  const options = useMemo(
    () => (clientSecret ? { clientSecret } : undefined),
    [clientSecret],
  );

  if (error) {
    return (
      <div className='mx-auto mt-12 max-w-xl rounded-md border border-red-200 bg-red-50 p-4 text-sm text-red-700'>
        {error}
      </div>
    );
  }

  if (!stripePromise || !options) {
    return (
      <div className='mx-auto mt-12 max-w-xl rounded-md border p-4 text-sm text-muted-foreground'>
        Loading checkout...
      </div>
    );
  }

  return (
    <div id='checkout'>
      <EmbeddedCheckoutProvider stripe={stripePromise} options={options}>
        <EmbeddedCheckout />
      </EmbeddedCheckoutProvider>
    </div>
  );
}
