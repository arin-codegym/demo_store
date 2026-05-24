export async function verifyPaymentServer(sessionId: string) {
  await fetch(
    `${process.env.API_EXTERNAL}/payment/verify?sessionId=${sessionId}`,
    {
      method: 'GET',
      cache: 'no-store', // 🔥 quan trọng
    },
  );
}
