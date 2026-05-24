'use server';

import { METHODS } from 'http';

const BACKEND_URL = process.env.API_EXTERNAL;
export const fetchOrders = () => {};
export async function verifyPayment(sessionId: string) {
  const response = await fetch(
    `/api/spingserver/payment/verify?sessionId=${sessionId}`,
    {
      method: 'GET',
    },
  );

  //   return response.data
}
