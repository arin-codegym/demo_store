import { dehydrate, QueryClient } from '@tanstack/react-query';
import axios from 'axios';
import OrdersClient from './OrdersClient';
import { verifyPaymentServer } from '@/lib/api/payment';
import { fetchOrdersServer } from '@/lib/api/orders';
// async function fetchOrders() {
//   const res = await axios.get('/api/spingserver/orders');
//   return res.data;
// }

// async function verifyPayment(sessionId: string) {
//   await axios.get(`/api/spingserver/payment/verify?sessionId=${sessionId}`);
// }
async function OrdersPage({
  searchParams,
}: {
  searchParams: { session_id?: string };
}) {
  const params = await searchParams;
  const sessionId = params.session_id;
  // 🔥 STEP 1: verify trước
  if (sessionId) {
    await verifyPaymentServer(sessionId);
  }
  // 🔥 STEP 2: prefetch orders
  const queryClient = new QueryClient();

  await queryClient.prefetchQuery({
    queryKey: ['orders'],
    queryFn: fetchOrdersServer,
  });
  /* Kiến trúc client-side đúng bản chất nhưng chuyển qua hybrid cho đồng bộ */
  // const { data: orders, isLoading } = useQuery({
  //   queryKey: ['orders'],
  //   queryFn: fetchOrders,
  // });
  // // 🔥 Verify nếu có session_id
  // useEffect(() => {
  //   if (!sessionId) return;

  //   async function handleVerify() {
  //     try {
  //       if (sessionId) {
  //         await verifyPayment(sessionId);

  //         // refresh orders sau khi verify
  //         await queryClient.invalidateQueries({ queryKey: ['orders'] });
  //       }
  //     } catch (error) {
  //       console.error('Verify failed:', error);
  //     }
  //   }

  //   handleVerify();
  // }, [sessionId, queryClient]);
  const dehydratedState = dehydrate(queryClient);
  return <OrdersClient dehydratedState={dehydratedState} />;
  // return (

  // <>
  //   <SectionTitle text='Your Orders' />
  //   <div>
  //     <Table>
  //       <TableCaption>Total orders : {orders.length}</TableCaption>
  //       <TableHeader>
  //         <TableRow>
  //           <TableHead>Products</TableHead>
  //           <TableHead>Order Total</TableHead>
  //           <TableHead>Tax</TableHead>
  //           <TableHead>Shipping</TableHead>
  //           <TableHead>Date</TableHead>
  //         </TableRow>
  //       </TableHeader>
  //       <TableBody>
  //         {orders.map((order:any) => {
  //           const { id, products, orderTotal, tax, shipping, createdAt } =
  //             order;

  //           return (
  //             <TableRow key={order.id}>
  //               <TableCell>{products}</TableCell>
  //               <TableCell>{formatCurrency(orderTotal)}</TableCell>
  //               <TableCell>{formatCurrency(tax)}</TableCell>
  //               <TableCell>{formatCurrency(shipping)}</TableCell>
  //               <TableCell>{formatDate(createdAt)}</TableCell>
  //             </TableRow>
  //           );
  //         })}
  //       </TableBody>
  //     </Table>
  //   </div>
  // </>
  // );
}
export default OrdersPage;
