'use client';
import Logo from './Logo';
import LinksDropdown from './LinksDropdown';
import DarkMode from './DarkMode';
import CartButton from './CartButton';
import NavSearch from './NavSearch';
import Container from '../global/Container';
import { Suspense } from 'react';
import { UserProvider } from '../context/UserProvider';
import { useCurrentUser } from '@/query/auth/useCurrentUser';
import { NotificationBell } from './NotificationBell';
type NavbarProps = {
  showSearch?: boolean;
};
function Navbar({ showSearch = true }: NavbarProps) {
  const { data: user, isLoading, status } = useCurrentUser();
  return (
    <nav className='border-b bg-background'>
      <Container className='flex flex-col gap-4 px-5 py-5 sm:flex-row sm:flex-wrap sm:items-center sm:justify-between sm:px-8 sm:py-8'>
        <div className='flex items-center justify-between sm:block'>
          <Logo />
        </div>

        <div className='w-full sm:w-auto sm:flex-1 sm:px-4 lg:max-w-md lg:px-0'>
          {showSearch ? (
            <Suspense>
              <NavSearch />
            </Suspense>
          ) : (
            <div />
          )}
        </div>

        <div className='flex items-center justify-center gap-3 sm:justify-end sm:gap-4'>
          <NotificationBell />
          <UserProvider user={user}>
            {showSearch ? <CartButton /> : <div />}

            <DarkMode />
            <Suspense>
              <LinksDropdown />
            </Suspense>
          </UserProvider>
        </div>
      </Container>
    </nav>
  );
}
export default Navbar;
