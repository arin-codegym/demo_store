import { dehydrate, QueryClient } from '@tanstack/react-query';
import OrdersClient from './OrdersClient';
import { verifyPaymentServer } from '@/lib/api/payment';
import { fetchOrdersServer } from '@/lib/api/orders';

async function OrdersPage({
  searchParams,
}: {
  searchParams: { session_id?: string };
}) {
  const params = await searchParams;
  const sessionId = params.session_id;

  // Verify Stripe redirect results before hydrating the order list so the first
  // client render sees the updated paid state.
  if (sessionId) {
    await verifyPaymentServer(sessionId);
  }

  const queryClient = new QueryClient();

  await queryClient.prefetchQuery({
    queryKey: ['orders'],
    queryFn: fetchOrdersServer,
  });

  const dehydratedState = dehydrate(queryClient);
  return (
    <OrdersClient
      dehydratedState={dehydratedState}
      paymentSessionId={sessionId ?? null}
    />
  );
}
export default OrdersPage;
