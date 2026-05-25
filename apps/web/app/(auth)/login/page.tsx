'use client';
import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { Input } from '@/components/ui/input';
import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { usePathname, useRouter } from 'next/navigation';
import { LuEyeOff } from 'react-icons/lu';
import { toast } from '@/components/ui/use-toast';
import { LuLoader } from 'react-icons/lu';
import { useLogin } from '@/query/auth/useLogin';
import { useCurrentUser } from '@/query/auth/useCurrentUser';

export type LoginState = {
  success: boolean;
  error: string | null;
  username: string;
};

const initState: LoginState = {
  success: false,
  error: null,
  username: '',
};

/**
 * LoginPage: Sử dụng Server Actions và useActionState (React 19). userFormState(cũ) đã bị deprecate. làm chức năng đăng nhập.
 * - formAction: Là hàm dispatch nhận vào formData (trả về void).
 * - state: Nhận kết quả từ logic return của loginAction (prevState).
 */
function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  // const pathname = usePathname();
  const route = useRouter();
  const { data: currentUser, isLoading } = useCurrentUser();

  const loginMutation = useLogin();
  const isPending = loginMutation.isPending;

  useEffect(() => {
    if (!isLoading && currentUser) {
      route.push('/');
    }
  }, [, currentUser, isLoading]);

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loginMutation.mutate(
      { username, password },
      {
        // ✅ Gọi toast ngay tại đây
        onSuccess: () => {
          toast({
            description: 'Đăng nhập thành công!',
            duration: 1000, // Chạy trong 3 giây
          });
        },
        // ✅ Và hiển thị lỗi tại đây
        onError: (error) => {
          toast({
            variant: 'destructive',
            description: error.message,
          });
        },
      },
    );
  };

  return (
    // <form action={formAction}>
    <form onSubmit={onSubmit}>
      <Card className='w-full max-w-sm'>
        <CardHeader>
          <CardTitle>Login to your account</CardTitle>
          <CardDescription>
            Enter your username below to login to your account
          </CardDescription>
          <CardAction>
            <Button asChild variant='link'>
              <Link href='/register'>Sign Up</Link>
            </Button>
          </CardAction>
        </CardHeader>
        <CardContent>
          <div className='flex flex-col gap-6'>
            <div className='grid gap-2'>
              <Label htmlFor='username'>User Name</Label>
              <Input
                id='username'
                name='username'
                type='text'
                placeholder='Enter your username'
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                disabled={isPending}
              />
            </div>
            <div className='grid gap-2'>
              <div className='flex items-center'>
                <Label htmlFor='password'>Password</Label>
                {/* <a
                  href='#'
                  className='ml-auto inline-block text-sm underline-offset-4 hover:underline'
                >
                  Forgot your password?
                </a> */}
                <Link
                  href='/'
                  className='ml-auto inline-block text-sm underline-offset-4 hover:underline'
                >
                  Forgot your password?
                </Link>
              </div>
              <div className='relative flex items-center group'>
                <Input
                  id='password'
                  name='password'
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={isPending}
                />
                <Button
                  type='button'
                  className='absolute right-[-4px] p-1 rounded-md text-slate-400 hover:text-slate-900 hover:bg-slate-100 transition-all z-20 focus:outline-none'
                  // onClick={(e) => {
                  //   e.preventDefault();
                  //   setShowPassword(!showPassword);
                  // }}
                  onClick={() => setShowPassword((v) => !v)}
                >
                  {showPassword ? (
                    <LuEyeOff size={18} />
                  ) : (
                    <LuEyeOff size={18} />
                  )}
                </Button>
              </div>
            </div>
          </div>
        </CardContent>
        <CardFooter className='flex-col gap-2'>
          <Button type='submit' className='w-full' disabled={isPending}>
            {isPending ? (
              <>
                <LuLoader className='mr-2 h-4 w-4 animate-spin' />
                Đang xử lý...
              </>
            ) : (
              'Login'
            )}
          </Button>
          <Button
            variant='outline'
            className='w-full'
            type='button'
            onClick={() => {
              window.location.href = '/api/backend/oauth2/authorization/google';
            }}
          >
            {/* SVG Logo Google chuẩn */}
            <svg
              xmlns='http://www.w3.org/2000/svg'
              viewBox='0 0 48 48'
              width='20px'
              height='20px'
            >
              <path
                fill='#FFC107'
                d='M43.611,20.083H42V20H24v8h11.303c-1.649,4.657-6.08,8-11.303,8c-6.627,0-12-5.373-12-12c0-6.627,5.373-12,12-12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C12.955,4,4,12.955,4,24s8.955,20,20,20s20-8.955,20-20C44,22.659,43.862,21.35,43.611,20.083z'
              />
              <path
                fill='#FF3D00'
                d='M6.306,14.691l6.571,4.819C14.655,15.108,18.961,12,24,12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C16.318,4,9.656,8.337,6.306,14.691z'
              />
              <path
                fill='#4CAF50'
                d='M24,44c5.166,0,9.86-1.977,13.409-5.192l-6.19-5.238C29.211,35.091,26.715,36,24,36c-5.202,0-9.619-3.317-11.283-7.946l-6.522,5.025C9.505,39.556,16.227,44,24,44z'
              />
              <path
                fill='#1976D2'
                d='M43.611,20.083H42V20H24v8h11.303c-0.792,2.237-2.231,4.166-4.087,5.571c0.001-0.001,0.002-0.001,0.003-0.002l6.19,5.238C36.971,39.205,44,34,44,24C44,22.659,43.862,21.35,43.611,20.083z'
              />
            </svg>
            Login with Google
          </Button>
        </CardFooter>
      </Card>
    </form>
  );
}

export default LoginPage;
