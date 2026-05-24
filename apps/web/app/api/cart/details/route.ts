import { proxyJson } from '@/lib/proxy-json-route';
import { NextRequest } from 'next/server';

export async function GET(req: NextRequest) {
  return proxyJson({
    req,
    endpoint: '/cart/details',
    method: 'GET',
  });
}
