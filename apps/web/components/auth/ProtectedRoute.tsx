// 'use client';
// import { useAuthStore } from '../../lib/store/authStore';
// import { useRouter } from 'next/navigation';
// import { useEffect } from 'react';

// interface ProtectedRouteProps {
//   children: React.ReactNode;
//   allowedRoles?: ('ADMIN' | 'USER')[]; // Danh sách các role được phép truy cập
// }

// export default function ProtectedRoute({
//   children,
//   allowedRoles,
// }: ProtectedRouteProps) {
//   const { isAuthenticated, user } = useAuthStore();
//   const router = useRouter();

//   useEffect(() => {
//     // Logic kiểm tra quyền truy cập
//     if (!isAuthenticated) {
//       router.push('/login');
//     } else if (allowedRoles && user && !allowedRoles.includes(user.role)) {
//       router.push('/unauthorized'); // Hoặc trang chủ
//     }
//   }, [isAuthenticated, user, router, allowedRoles]);

//   // Trong lúc chờ useEffect chạy hoặc nếu không đủ quyền, không hiển thị gì cả
//   if (
//     !isAuthenticated ||
//     (allowedRoles && user && !allowedRoles.includes(user.role))
//   ) {
//     return (
//       <div className='flex justify-center p-10'>
//         Đang kiểm tra quyền truy cập... ⏳
//       </div>
//     );
//   }

//   return <>{children}</>;
// }
