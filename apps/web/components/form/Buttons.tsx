'use client';

import { ReloadIcon } from '@radix-ui/react-icons';
import { useFormStatus } from 'react-dom';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { FaRegHeart, FaHeart } from 'react-icons/fa';
import { LuTrash2, LuSquarePen } from 'react-icons/lu';

type btnSize = 'default' | 'lg' | 'sm';

type SubmitButtonProps = {
  className?: string;
  text?: string;
  size?: btnSize;
};

export function SubmitButton({
  className = '',
  text = 'submit',
  size = 'lg',
}: SubmitButtonProps) {
  const { pending } = useFormStatus();

  return (
    <Button
      type='submit'
      disabled={pending}
      className={cn('capitalize', className)}
      size={size}
    >
      {pending ? (
        <>
          <ReloadIcon className='mr-2 h-4 w-4 animate-spin' />
          Please wait...
        </>
      ) : (
        text
      )}
    </Button>
  );
}

type actionType = 'edit' | 'delete';
export const IconButton = ({ actionType }: { actionType: actionType }) => {
  const { pending } = useFormStatus();

  const renderIcon = () => {
    switch (actionType) {
      case 'edit':
        return <LuSquarePen />;
      case 'delete':
        return <LuTrash2 />;
      default:
        const never: never = actionType;
        throw new Error(`Invalid action type: ${never}`);
    }
  };

  return (
    <Button
      type='submit'
      size='icon'
      variant='link'
      className='p-2 cursor-pointer'
    >
      {pending ? <ReloadIcon className=' animate-spin' /> : renderIcon()}
    </Button>
  );
};

export const CardSignInButton = () => {
  return (
    // <SignInButton mode='modal'>
    <Button
      type='button'
      size='icon'
      variant='outline'
      className='p-2 cursor-pointer'
      asChild
    >
      <FaRegHeart />
    </Button>
  );
};

export const CardSubmitButton = ({ isFavorite }: { isFavorite: boolean }) => {
  const { pending } = useFormStatus();
  return (
    <Button
      type='submit'
      size='icon'
      variant='outline'
      className=' p-2 cursor-pointer'
    >
      {pending ? (
        <ReloadIcon className=' animate-spin' />
      ) : isFavorite ? (
        <FaHeart />
      ) : (
        <FaRegHeart />
      )}
    </Button>
  );
};

type FavoriteIconButtonProps = {
  isFavorite: boolean;
  isPending?: boolean;
};

export const FavoriteIconButton = ({
  isFavorite,
  isPending = false,
}: FavoriteIconButtonProps) => {
  return (
    <span className='inline-flex items-center justify-center'>
      {isPending ? (
        <ReloadIcon className='h-4 w-4 animate-spin' />
      ) : isFavorite ? (
        <FaHeart className='h-4 w-4 text-red-500' />
      ) : (
        <FaRegHeart className='h-4 w-4 text-gray-700' />
      )}
    </span>
  );
};

export const ProductSignInButton = () => {
  return (
    // <SignInButton mode='modal'>
    <Button type='button' size='default' className='mt-8'>
      Please Sign In
    </Button>
    // </SignInButton>
  );
};
