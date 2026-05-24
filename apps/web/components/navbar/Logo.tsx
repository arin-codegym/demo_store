import Link from 'next/link';
import React from 'react';
import { Button } from '../ui/button';
import { VscCode } from 'react-icons/vsc';
function Logo() {
  return (
    <Button size='icon' asChild>
      <Link href='/api/auth/bootstrap?next=/'>
        <VscCode className='w-6 h-6' />
      </Link>
    </Button>
  );
}

export default Logo;
