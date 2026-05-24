//app\api\admin\product\create\route.ts
import { proxyPostJson } from '@/lib/proxy-json-route';
import { NextRequest } from 'next/server';

export async function POST(req: NextRequest) {
  return proxyPostJson(req, '/product/createProduct');
}
