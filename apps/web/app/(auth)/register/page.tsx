'use client';

import Link from 'next/link';
import { useState } from 'react';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { toast } from '@/components/ui/use-toast';
import { LuLoader } from 'react-icons/lu';

type RegisterPayload = {
  userName: string;
  fullName: string;
  email: string;
  password: string;
};

export default function RegisterPage() {
  const [values, setValues] = useState<RegisterPayload>({
    userName: '',
    fullName: '',
    email: '',
    password: '',
  });
  const [isPending, setIsPending] = useState(false);
  const [submittedEmail, setSubmittedEmail] = useState('');

  const updateField =
    (field: keyof RegisterPayload) =>
    (event: React.ChangeEvent<HTMLInputElement>) => {
      setValues((current) => ({
        ...current,
        [field]: event.target.value,
      }));
    };

  const onSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setIsPending(true);

    try {
      const res = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(values),
      });
      const data = await res.json().catch(() => ({}));

      if (!res.ok) {
        throw new Error(data?.message || 'Registration failed');
      }

      setSubmittedEmail(values.email);
      toast({ description: data?.message || 'Registration successful' });
    } catch (error) {
      toast({
        variant: 'destructive',
        description:
          error instanceof Error ? error.message : 'Registration failed',
      });
    } finally {
      setIsPending(false);
    }
  };

  if (submittedEmail) {
    return (
      <Card className='w-full max-w-sm'>
        <CardHeader>
          <CardTitle>Check your email</CardTitle>
          <CardDescription>
            We sent an activation link to {submittedEmail}.
          </CardDescription>
        </CardHeader>
        <CardContent className='text-sm text-muted-foreground'>
          Open the email and activate your account before logging in.
        </CardContent>
        <CardFooter>
          <Button asChild className='w-full'>
            <Link href='/login'>Back to login</Link>
          </Button>
        </CardFooter>
      </Card>
    );
  }

  return (
    <form onSubmit={onSubmit}>
      <Card className='w-full max-w-sm'>
        <CardHeader>
          <CardTitle>Create an account</CardTitle>
          <CardDescription>
            Register a user account and activate it by email.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className='flex flex-col gap-4'>
            <div className='grid gap-2'>
              <Label htmlFor='fullName'>Full name</Label>
              <Input
                id='fullName'
                value={values.fullName}
                onChange={updateField('fullName')}
                disabled={isPending}
                required
              />
            </div>
            <div className='grid gap-2'>
              <Label htmlFor='userName'>Username</Label>
              <Input
                id='userName'
                value={values.userName}
                onChange={updateField('userName')}
                disabled={isPending}
                minLength={3}
                required
              />
            </div>
            <div className='grid gap-2'>
              <Label htmlFor='email'>Email</Label>
              <Input
                id='email'
                type='email'
                value={values.email}
                onChange={updateField('email')}
                disabled={isPending}
                required
              />
            </div>
            <div className='grid gap-2'>
              <Label htmlFor='password'>Password</Label>
              <Input
                id='password'
                type='password'
                value={values.password}
                onChange={updateField('password')}
                disabled={isPending}
                minLength={6}
                required
              />
            </div>
          </div>
        </CardContent>
        <CardFooter className='flex-col gap-2'>
          <Button type='submit' className='w-full' disabled={isPending}>
            {isPending ? (
              <>
                <LuLoader className='mr-2 h-4 w-4 animate-spin' />
                Creating...
              </>
            ) : (
              'Create account'
            )}
          </Button>
          <Button asChild variant='link' className='w-full'>
            <Link href='/login'>Already have an account?</Link>
          </Button>
        </CardFooter>
      </Card>
    </form>
  );
}
