import Link from 'next/link';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';

type ActivatePageProps = {
  searchParams: Promise<{
    token?: string;
  }>;
};

export default async function ActivatePage({ searchParams }: ActivatePageProps) {
  const { token } = await searchParams;
  let title = 'Activation failed';
  let description = 'Activation link is invalid or has expired.';
  let success = false;

  if (token) {
    const res = await fetch(
      `${process.env.API_EXTERNAL}/auth/activate?${new URLSearchParams({
        token,
      })}`,
      { cache: 'no-store' },
    );
    const data = await res.json().catch(() => ({}));
    success = res.ok;
    title = res.ok ? 'Account activated' : 'Activation failed';
    description =
      data?.message ||
      (res.ok
        ? 'Your account is ready. You can now log in.'
        : 'Activation link is invalid or has expired.');
  }

  return (
    <Card className='w-full max-w-sm'>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent className='text-sm text-muted-foreground'>
        {success
          ? 'Use your username and password to continue.'
          : 'Request a new registration email if this link no longer works.'}
      </CardContent>
      <CardFooter>
        <Button asChild className='w-full'>
          <Link href={success ? '/login' : '/register'}>
            {success ? 'Go to login' : 'Register again'}
          </Link>
        </Button>
      </CardFooter>
    </Card>
  );
}
