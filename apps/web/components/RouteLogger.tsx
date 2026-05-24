'use client';
import { useEffect } from 'react';
import { usePathname, useSearchParams } from 'next/navigation';

export default function RouteLogger() {
  const pathname = usePathname();
  const sp = useSearchParams();

  useEffect(() => {
    console.log('🧭 ROUTE =>', pathname + (sp?.toString() ? `?${sp}` : ''));
  }, [pathname, sp]);

  return null;
}
