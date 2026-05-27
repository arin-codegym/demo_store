'use client';

import SectionTitle from '@/components/global/SectionTitle';
import {
  HydrationBoundary,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { fetchWithAuth } from '@/lib/fetchWithAuth.client';
import { formatCurrency, formatDate } from '@/utils/format';
import { useEffect } from 'react';
import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';

function OrdersClient({
  dehydratedState,
  paymentSessionId,
}: {
  dehydratedState: any;
  paymentSessionId?: string | null;
}) {
  return (
    <HydrationBoundary state={dehydratedState}>
      <OrdersContent paymentSessionId={paymentSessionId} />
    </HydrationBoundary>
  );
}

function OrdersContent({
  paymentSessionId,
}: {
  paymentSessionId?: string | null;
}) {
  const queryClient = useQueryClient();
  const { data: orders = [], isLoading } = useQuery({
    queryKey: ['orders'],
    queryFn: async () => {
      const res = await fetchWithAuth('/api/order', {
        credentials: 'include',
      });
      if (!res.ok) {
        return [];
      }
      const data = await res.json();
      return data.orders ?? [];
    },
  });

  useEffect(() => {
    if (!paymentSessionId) return;

    void queryClient.invalidateQueries({ queryKey: ['cart-count'] });
    void queryClient.invalidateQueries({ queryKey: ['cart'] });
    void queryClient.invalidateQueries({ queryKey: ['orders'] });
  }, [paymentSessionId, queryClient]);

  if (isLoading) return <div>Loading...</div>;

  return (
    <>
      <SectionTitle text='Your Orders' />
      <div>
        <Table>
          <TableCaption>Total orders : {orders.length}</TableCaption>
          <TableHeader>
            <TableRow>
              <TableHead>Products</TableHead>
              <TableHead>Order Total</TableHead>
              <TableHead>Tax</TableHead>
              <TableHead>Shipping</TableHead>
              <TableHead>Date</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {orders.map((order: any) => {
              const {
                orderId,
                productsCount,
                orderTotal,
                tax,
                shipping,
                createdAt,
              } = order;

              return (
                <TableRow key={orderId}>
                  <TableCell>{productsCount}</TableCell>
                  <TableCell>{formatCurrency(orderTotal)}</TableCell>
                  <TableCell>{formatCurrency(tax)}</TableCell>
                  <TableCell>{formatCurrency(shipping)}</TableCell>
                  <TableCell>{formatDate(createdAt)}</TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </div>
    </>
  );
}

export default OrdersClient;
