'use client';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from '@/components/ui/dropdown-menu';
import { LuAlignLeft } from 'react-icons/lu';
import Link from 'next/link';
import { Button } from '../ui/button';
import { links } from '@/utils/links';
import UserIcon from './UserIcon';
import { Suspense } from 'react';
import { logoutAction } from '@/server/auth-action';
import { useUser } from '../context/UserProvider';
import { useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';

function LinksDropdown() {
  const user = useUser();
  const qc = useQueryClient();
  const isAdmin = user?.roles?.includes('ROLE_ADMIN') ?? false;
  const route = useRouter();
  const handleLogout = async () => {
    try {
      // Bước 1: Gọi API xóa cookie ở Spring Boot (nếu có)
      // await authApi.logout();

      // Bước 2: Xóa dữ liệu ở Zustand
      await logoutAction();
      qc.clear(); // nếu muốn reset toàn bộ cache
      // Bước 3: Về trang chủ

      route.refresh();
    } catch (error) {
      console.error('Lỗi khi đăng xuất:', error);
    } finally {
      qc.clear();
      route.refresh();
    }
  };

  if (!user) {
    return (
      <DropdownMenu modal={false}>
        <DropdownMenuTrigger asChild>
          <Button variant='outline' className='flex h-9 gap-2 px-3 sm:h-10 sm:max-w-[100px] sm:gap-4'>
            <LuAlignLeft className='h-5 w-5 sm:h-6 sm:w-6' />
            <UserIcon />
          </Button>
        </DropdownMenuTrigger>

        <DropdownMenuContent className='w-48' align='start' sideOffset={10}>
          <DropdownMenuItem asChild>
            <Link href='/login' className='w-full'>
              Login
            </Link>
          </DropdownMenuItem>
          <DropdownMenuSeparator />
          <DropdownMenuItem asChild>
            <Link href='/register' className='w-full'>
              Register
            </Link>
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant='outline' className='flex h-9 gap-2 px-3 sm:h-10 sm:max-w-[100px] sm:gap-4'>
          <LuAlignLeft className='h-5 w-5 sm:h-6 sm:w-6' />
          <Suspense>
            <UserIcon />
          </Suspense>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className='w-48' align='start' sideOffset={10}>
        <>
          {links.map((link) => {
            if (link.label === 'dashboard' && !isAdmin) return null;
            return (
              <DropdownMenuItem key={link.href} asChild>
                <Link href={link.href} className='capitalize w-full'>
                  {link.label}
                </Link>
              </DropdownMenuItem>
            );
          })}
          <DropdownMenuSeparator />
          <DropdownMenuItem onSelect={handleLogout}>
            <button className='w-full text-left'>Logout</button>
          </DropdownMenuItem>
        </>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
export default LinksDropdown;
