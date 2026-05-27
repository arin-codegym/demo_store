import { cookies } from 'next/headers';

export async function verifyPaymentServer(sessionId: string) {
  const cookieStore = await cookies();
  const cookieHeader = cookieStore.toString();

  const response = await fetch(
    `${process.env.API_EXTERNAL}/payment/verify?sessionId=${encodeURIComponent(
      sessionId,
    )}`,
    {
      method: 'GET',
      headers: {
        Cookie: cookieHeader || '',
      },
      cache: 'no-store',
    },
  );

  if (!response.ok) {
    const text = await response.text();
    console.error(
      `verifyPaymentServer failed: ${response.status} ${text || response.statusText}`,
    );
  }
}
