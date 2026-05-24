// /api/cart/count
import { proxyGetJson } from '@/lib/proxy-json-route';
import { NextRequest } from 'next/server';

export async function GET(request: NextRequest) {
  return proxyGetJson(request, '/cart/count');
}
