export const dynamic = 'force-dynamic';
import { fetchCartDetails } from '@/action/cart-action';
import { dehydrate, QueryClient } from '@tanstack/react-query';
import CartClient from '@/components/cart/CartClient';

async function CartPage() {
  /* initialData = data server → props → client → nhét vào cache

dùng prefetch = data server → cache → serialize → hydrate client */
  // const initialCart = await fetchCartDetails();
  const queryClient = new QueryClient();

  await queryClient.prefetchQuery({
    queryKey: ['cart'],
    queryFn: fetchCartDetails,
  });

  const dehydratedState = dehydrate(queryClient);

  return <CartClient dehydratedState={dehydratedState} />;
}
export default CartPage;
