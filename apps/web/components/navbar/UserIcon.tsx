'use client';

import { useState } from 'react';
import { LuUser } from 'react-icons/lu';

import { useUser } from '../context/UserProvider';

function isUsableAvatarSrc(src: string) {
  return (
    src.startsWith('/') ||
    src.startsWith('http://') ||
    src.startsWith('https://') ||
    src.startsWith('data:image/') ||
    src.startsWith('blob:')
  );
}

function UserIcon() {
  const user = useUser();
  const [failedSrc, setFailedSrc] = useState<string | null>(null);
  const profileImage = user?.avatarUrl?.trim() ?? '';

  const shouldShowAvatar =
    profileImage &&
    isUsableAvatarSrc(profileImage) &&
    failedSrc !== profileImage;

  if (shouldShowAvatar) {
    return (
      <img
        src={profileImage}
        alt={user?.fullName || user?.username || 'User profile'}
        width={24}
        height={24}
        referrerPolicy='no-referrer'
        className='h-6 w-6 rounded-full object-cover'
        onError={() => setFailedSrc(profileImage)}
      />
    );
  }

  return (
    <span className='flex h-6 w-6 items-center justify-center rounded-full bg-primary text-white'>
      <LuUser className='h-4 w-4' />
    </span>
  );
}

export default UserIcon;
