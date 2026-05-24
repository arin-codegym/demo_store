//app\api\admin\users\route.ts
import { proxyGetJson } from '@/lib/proxy-json-route';
import { NextRequest } from 'next/server';
export async function GET(req: NextRequest) {
  // const cookieHeader = req.headers.get('cookie') || '';
  // return fetch(`${process.env.API_EXTERNAL}/admin/users`, {
  //   method: 'GET',
  //   headers: {
  //     // quan trọng nhất:
  //     cookie: cookieHeader,
  //     accept: 'application/json',
  //   },
  // });
  return proxyGetJson(req, '/admin/users');
}
