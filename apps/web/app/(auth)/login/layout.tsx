import FormContainer from '@/components/form/FormContainer';
import Link from 'next/link';

export default function AuthLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    // Thẻ cha ngoài cùng: Dùng Flex để đưa toàn bộ nội dung vào giữa màn hình
    <div className='min-h-screen w-full bg-slate-50 flex items-center justify-center p-4'>
      {/* Thẻ bao quanh nội dung: Giới hạn độ rộng để link và form thẳng hàng trái */}
      <div className='w-full max-w-md flex flex-col items-start'>
        {/* Link quay về: Luôn bám lề trái của "max-w-md" */}
        <Link
          href='/'
          className='mb-4 flex items-center text-sm font-medium text-slate-500 hover:text-slate-800 transition-colors'
        >
          <svg
            className='mr-2 h-4 w-4'
            fill='none'
            viewBox='0 0 24 24'
            stroke='currentColor'
          >
            <path
              strokeLinecap='round'
              strokeLinejoin='round'
              strokeWidth={2}
              d='M10 19l-7-7m0 0l7-7m-7 7h18'
            />
          </svg>
          Back to home
        </Link>

        {/* Form trắng (children) */}
        <div className='w-full bg-white rounded-2xl shadow-sm border border-slate-200 p-8'>
          {children}
        </div>
      </div>
    </div>
  );
}
