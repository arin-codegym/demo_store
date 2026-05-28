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
    return <div className='h-9 w-9 animate-pulse rounded-md bg-gray-100 sm:h-10 sm:w-10' />;
  }

  return (
    <Button
      asChild
      variant='outline'
      size='icon'
      className='relative flex h-9 w-9 items-center justify-center sm:h-10 sm:w-10'
    >
      <Link href='/cart'>
        <LuShoppingCart />
        <span className='absolute -right-2 -top-2 flex h-5 min-w-5 items-center justify-center rounded-full bg-primary px-1 text-xs text-white sm:-right-3 sm:-top-3 sm:h-6 sm:min-w-6'>
          {count}
        </span>
      </Link>
    </Button>
  );
}

export default CartButton;
