'use client';
import { useState } from 'react';
import SelectProductAmount from './SelectProductAmount';
import { Mode } from './SelectProductAmount';
import { ProductSignInButton } from '../form/Buttons';
import { useCurrentUser } from '@/query/auth/useCurrentUser';
import { Button } from '../ui/button';
import { ReloadIcon } from '@radix-ui/react-icons';
import { toast } from '../ui/use-toast';
import { useRouter } from 'next/navigation';
import { useAddToCart } from '@/query/cart/useAddToCart';

function AddToCart({ productId }: { productId: string }) {
  const [amount, setAmount] = useState(1);
  const { data: user, isLoading } = useCurrentUser();
  const isAuthenticated = !!user;
  const { mutate: addToCart, isPending } = useAddToCart();
  const router = useRouter();

  const handleAdd = () => {
    addToCart(
      { productId, amount },
      {
        onSuccess: () => {
          toast({ description: 'Đã thêm vào giỏ hàng' });
        },
        onError: () => {
          toast({
            variant: 'destructive',
            description: 'Vui lòng đăng nhập để tiếp tục',
          });
          router.push('/login');
        },
      },
    );
  };

  return (
    <div className='mt-4'>
      <SelectProductAmount
        mode={Mode.SingleProduct}
        amount={amount}
        setAmount={setAmount}
      />
      {isAuthenticated ? (
        <Button
          size='default'
          className='mt-8'
          onClick={handleAdd}
          disabled={isPending}
        >
          {isPending ? (
            <>
              <ReloadIcon className='mr-2 h-4 w-4 animate-spin' />
              Please wait...
            </>
          ) : (
            'Add to cart'
          )}
        </Button>
      ) : (
        <ProductSignInButton />
      )}
    </div>
  );
}
export default AddToCart;
