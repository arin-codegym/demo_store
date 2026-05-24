'use client';
// import { useState } from 'react';
import SelectProductAmount from '../single-product/SelectProductAmount';
import { Mode } from '../single-product/SelectProductAmount';
// import FormContainer from '../form/FormContainer';
// import { SubmitButton } from '../form/Buttons';
// import { removeCartItemAction, updateCartItemAction } from '@/utils/actions';
import { useToast } from '../ui/use-toast';
import { useUpdateCartItem } from '@/query/cart/useUpdateCartItem';
import { useCart } from '@/query/cart/useCart';
import { Button } from '../ui/button';
import { useRemoveCartItem } from '@/query/cart/useRemoveCartItem';

function ThirdColumn({ quantity, id }: { quantity: number; id: string }) {
  const { data } = useCart();
  const item = data?.cartDetails.cartItems.find(
    (i: any) => i.cartItemId === id,
  );
  // const [amount, setAmount] = useState(quantity);
  const { mutate, isPending } = useUpdateCartItem();
  const { mutate: removeMutate, isPending: removePending } =
    useRemoveCartItem();
  const { toast } = useToast();
  const handleAmountChange = async (value: number) => {
    // const previous = amount;
    mutate(
      {
        cartItemId: id,
        amount: value,
      },
      {
        onSuccess: (data) => {
          // setAmount(value);
          toast({ description: 'Cart updated' });
        },
        onError: () => {
          toast({ description: 'Update failed' });
          // setAmount(previous); // rollback
        },
      },
    );
  };

  return (
    <div className='md:ml-8'>
      <SelectProductAmount
        amount={item?.amount}
        setAmount={handleAmountChange}
        mode={Mode.CartItem}
        isLoading={isPending}
      />
      {/* <FormContainer action={removeCartItemAction}>
        <input type='hidden' name='id' value={id} />
        <SubmitButton size='sm' className='mt-4' text='remove' />
      </FormContainer> */}
      <Button
        className='mt-[1rem]'
        variant='destructive'
        onClick={() => removeMutate(id)}
      >
        Remove
      </Button>
    </div>
  );
}
export default ThirdColumn;
