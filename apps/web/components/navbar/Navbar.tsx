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
    <nav className='border-b '>
      <Container className='flex flex-col sm:flex-row  sm:justify-between sm:items-center flex-wrap gap-4 py-8'>
        <Logo />
        {showSearch ? (
          <Suspense>
            <NavSearch />
          </Suspense>
        ) : (
          <div />
        )}

        <div className='flex gap-4 items-center '>
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
