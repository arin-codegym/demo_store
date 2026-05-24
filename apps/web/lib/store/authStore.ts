import { User } from '@/utils/types';
import { clear } from 'console';
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { devtools } from 'zustand/middleware';
// interface User {
//   id: string;
//   username: string;
//   email: string;
//   avatarUrl?: string; // URL ảnh đại diện
//   fullName?: string;
//   roles: ['ROLE_ADMIN' | 'ROLE_MANAGER' | 'ROLE_USER'];
// }

interface AuthState {
  user: User | null;
  // accessToken: string | null;
  isAuthenticated: boolean;
  isHydrated: boolean;
  // Actions
  setUser: (user: User) => void;
  clearAuth: () => void;
  setHydrated: () => void;
}

// export const useAuthStore = create<AuthState>()(
//   devtools(
//     (set) => ({
//       user: null,
//       isAuthenticated: false,

//       setUser: (user) => {
//         set({
//           user,
//           isAuthenticated: true,
//         });
//         //  something();
//       },

//       clearAuth: () =>
//         set({
//           user: null,
//           isAuthenticated: false,
//         }),
//     }),
//     { name: 'auth-store' }, // tên hiện trong Redux DevTools
//   ),
// );

function something() {
  throw new Error('Function not implemented.');
}
export const useAuthStore = create<AuthState>()(
  devtools(
    persist(
      // Lưu trữ vào localStorage
      (set) => ({
        user: null,
        isAuthenticated: false,
        isHydrated: false,

        setUser: (user) =>
          set({
            user,
            isAuthenticated: true,
          }),

        clearAuth: () =>
          set({
            user: null,
            isAuthenticated: false,
            isHydrated: false,
          }),
        setHydrated: () => set({ isHydrated: true }),
      }),

      {
        name: 'auth-storage', // key localStorage
        storage: createJSONStorage(() => localStorage),
        onRehydrateStorage: () => (state) => {
          state?.setHydrated();
        },
      },
    ),
    { name: 'auth-store' },
  ),
);
