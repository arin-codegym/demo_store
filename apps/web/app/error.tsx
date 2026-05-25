'use client';

import { useEffect } from 'react';

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error('Lỗi từ Server Component:', error);
  }, [error]);

  return (
    <div className='flex flex-col items-center justify-center p-10 border-2 border-dashed border-red-200 rounded-lg bg-red-50'>
      <h2 className='text-xl font-bold text-red-600'>Đã có lỗi xảy ra!</h2>
      <p className='text-gray-600 mb-4'>
        {error.message || 'Không thể tải danh sách đánh giá lúc này.'}
      </p>
      <button
        onClick={() => reset()}
        className='px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700'
      >
        Thử lại
      </button>
    </div>
  );
}
