'use client';

import { useEffect, useRef } from 'react';
import { useAuthStore } from '@/lib/store/authStore';

/**
 * AuthInitializer: Chịu trách nhiệm đồng bộ dữ liệu từ Server Side
 * vào Zustand Store ở Client Side ngay khi trang được nạp.
 */
export default function AuthInitializer() {
  const { user, clearAuth, setUser, isHydrated } = useAuthStore();

  useEffect(() => {
    // đợi Zustand hydrate xong từ localStorage
    if (!isHydrated) return;
    const verifySession = async () => {
      try {
        const refresh = await fetch('/api/proxy/auth/refresh', {
          credentials: 'include',
        });
        // session chết → clear luôn
        if (!refresh.ok) {
          clearAuth();
          return;
        }
        // session sống → load lại user mới từ server
        const me = await fetch('/api/proxy/auth/me', {
          credentials: 'include',
        });
        if (!me.ok) {
          clearAuth();
          return;
        }
        const userData = await me.json();
        // update lại store nếu cần
        if (!user || user.userId !== userData.id) {
          setUser(userData);
        }
        // chạy background
      } catch (error) {
        clearAuth();
      }
    };
    verifySession();
  }, [isHydrated]);

  return null;
}
