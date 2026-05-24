'use client';
import { Button } from '../ui/button';
import Link from 'next/link';
import { LuShoppingCart } from 'react-icons/lu'; // Hook lấy thông tin user từ UserProvider

import { useCartCount } from '@/query/cart/useCartCount';

function CartButton() {
  const { data, isLoading } = useCartCount();
  const count = typeof data === 'number' ? data : 0;
  // const [isDataLoading, setIsDataLoading] = useState(true); // Trạng thái lấy data từ DB

  // useEffect(() => {
  //   const load = async () => {
  //     let res = await fetch('/api/cart/count', { credentials: 'include' });

  //     if (res.status === 401) {
  //       await fetch('/api/auth/refresh', {
  //         method: 'POST',
  //         credentials: 'include',
  //       });
  //       res = await fetch('/api/cart/count', { credentials: 'include' });
  //     }

  //     const data = await res.json();
  //     setCount(data.count);
  //   };

  //   // chạy khi mount
  //   load();
  //   // chạy khi quay lại tab
  //   window.addEventListener('focus', load);

  //   // chạy khi tab bị ẩn/hiện lại
  //   document.addEventListener('visibilitychange', () => {
  //     if (!document.hidden) load();
  //   });
  //   return () => {
  //     window.removeEventListener('focus', load);
  //     document.removeEventListener('visibilitychange', load);
  //   };
  // }, []); // ⭐ chỉ chạy khi user thay đổizy thay đổi

  // Logic Senior: Nếu Clerk đã load xong mà không có user,
  // thì con số hiển thị BẮT BUỘC phải là 0, bất kể state đang là bao nhiêu.
  // Logic hiển thị an toàn
  // const displayCount = !user ? 0 : numItemsInCart;
  // Nếu dữ liệu chưa về, hãy chủ động render ra Skeleton ở đây
  if (isLoading) {
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
