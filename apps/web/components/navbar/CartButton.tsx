'use client';
import { Button } from '../ui/button';
import Link from 'next/link';
import { LuShoppingCart } from 'react-icons/lu';

import { useCartCount } from '@/query/cart/useCartCount';
import { useCurrentUser } from '@/query/auth/useCurrentUser';

function CartButton() {
  const { data: currentUser, isLoading: isUserLoading } = useCurrentUser();
  const { data, isLoading } = useCartCount(Boolean(currentUser));
  const count = typeof data === 'number' ? data : 0;

  if (isUserLoading || isLoading) {
    return <div className='w-10 h-10 bg-gray-100 animate-pulse rounded-md' />;
  }

  return (
    <Button
      asChild
      variant='outline'
      size='icon'
      className='flex justify-center items-center relative'
    >
      <Link href='/cart'>
        <LuShoppingCart />
        <span className='absolute -top-3 -right-3 bg-primary text-white rounded-full h-6 w-6 flex items-center justify-center text-xs'>
          {count}
        </span>
      </Link>
    </Button>
  );
}

export default CartButton;
