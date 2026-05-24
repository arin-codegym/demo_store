import { useRouter } from 'next/navigation';

export function useDebugRouter() {
  const router = useRouter();

  return {
    ...router,
    replace: (href: string) => {
      if (href === '/login') {
        console.log('🚨 SOMEONE REPLACED TO /login', new Error().stack);
      }
      router.replace(href);
    },
    push: (href: string) => {
      console.log('push =>', href, new Error().stack);
      router.push(href);
    },
  };
}
