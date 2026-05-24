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

function AddToCart({ productId, price }: { productId: string; price: number }) {
  const [amount, setAmount] = useState(1);
  const mutation = useAddToCart();
  // const [mounted, setMounted] = useState(false);
  // // const { userId } = useAuth();
  // // 1. Sử dụng Store JWT của bạn
  const { data: user, isLoading } = useCurrentUser();
  const isAuthenticated = !!user;
  const { mutate: addToCart, isPending } = useAddToCart();
  // const addToCart = useAddToCart();
  const router = useRouter();
  const handleAdd = () => {
    // addToCart.mutate(
    //   { productId, amount },
    //   {
    //     onSuccess: () => {
    //       toast({ description: 'Đã thêm vào giỏ hàng' });
    //     },
    //     onError: () => {
    //       toast({
    //         variant: 'destructive',
    //         description: 'Vui lòng đăng nhập để tiếp tục',
    //       });
    //       router.push('/login');
    //     },
    //   },
    // );
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
  // const actionWithData = addToCartAction.bind(null, productId, amount, price);
  // // 2. Hydration Guard
  // useEffect(() => {
  //   setMounted(true);
  // }, []);
  return (
    <div className='mt-4'>
      <SelectProductAmount
        mode={Mode.SingleProduct}
        amount={amount}
        setAmount={setAmount}
      />
      {isAuthenticated ? (
        // <FormContainer action={actionWithData}>
        //   {/* <input type='hidden' name='productId' value={productId} />
        //   <input type='hidden' name='amount' value={amount} />
        //   <input type='hidden' name='price' value={price} /> */}
        //   <SubmitButton text='add to cart' size='default' className='mt-8' />
        // </FormContainer>
        <Button
          size='default'
          className='mt-8'
          onClick={handleAdd}
          disabled={isPending}
        >
          {/* {isPending ? 'Adding...' : 'Add to cart'} */}
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
