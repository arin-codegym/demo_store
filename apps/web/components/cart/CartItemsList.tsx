'use client';
import { Card } from '@/components/ui/card';
import { FirstColumn, SecondColumn, FourthColumn } from './CartItemColumns';
import ThirdColumn from './ThirdColumn';
import { CartItemWithProduct } from '@/utils/types';
function CartItemsList({ cartItems }: { cartItems: CartItemWithProduct[] }) {
  return (
    <div>
      {cartItems.map((cartItem: any) => {
        const { cartItemId, amount } = cartItem;
        const { productId, image, name, company, price } = cartItem.product;
        return (
          <Card
            key={cartItemId}
            className='flex flex-col gap-y-4 md:flex-row flex-wrap p-6 mb-8 gap-x-4'
          >
            <FirstColumn image={image} name={name} />
            <SecondColumn name={name} company={company} productId={productId} />
            <ThirdColumn id={cartItemId} quantity={amount} />
            <FourthColumn price={price} />
          </Card>
        );
      })}
    </div>
  );
}
export default CartItemsList;
