'use client';
import { LuUser } from 'react-icons/lu';
import Image from 'next/image';
import { useUser } from '../context/UserProvider';
import { useAuthStore } from '@/lib/store/authStore';
import { useEffect, useState } from 'react';

function UserIcon() {
  // const user = useAuthStore((state) => state.user);
  const user = useUser();
  // Trạng thái kiểm tra xem component đã được nạp ở Client chưa
  // const [mounted, setMounted] = useState(false);
  const [failed, setFailed] = useState(false);
  const profileImage = user?.avatarUrl?.trim();
  const src = !failed && profileImage ? profileImage : '/default-avatar.png';
  // useEffect(() => {
  //   // Đánh dấu đã mounted để tránh lỗi Hydration Mismatch
  //   setMounted(true);
  // }, []);
  // Giả sử Spring Boot trả về trường 'avatar' hoặc 'imageUrl'
  // const profileImage = user?.avatarUrl;
  // if (profileImage)
  return profileImage || failed ? (
    // <Image
    //   src={profileImage}
    //   alt={user?.fullName || 'User profile'}
    //   width={24}
    //   height={24}
    //   className='rounded-full object-cover h-6 w-6'
    // />
    <img
      src={src}
      alt={user?.fullName || 'User profile'}
      width={24}
      height={24}
      className='rounded-full object-cover h-6 w-6'
      onError={
        (e) => setFailed(true)
        //   {
        //   // (e.currentTarget as HTMLImageElement).src = '/default-avatar.png';
        //   // const img = e.currentTarget;
        //   // // chặn loop: chỉ fallback 1 lần
        //   // img.onerror = null;
        //   // img.src = '/default-avatar.png';

        // }
      }
    />
  ) : (
    <LuUser className='w-6 h-6 bg-primary rounded-full text-white' />
  );
  // return <LuUser className='w-6 h-6 bg-primary rounded-full text-white' />;
}
export default UserIcon;
