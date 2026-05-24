import { proxyPatchJson } from '@/lib/proxy-json-route';
import { NextRequest, NextResponse } from 'next/server';

// URL backend thật (Spring / Nest)
const BACKEND_URL = process.env.API_EXTERNAL;

/* proxy sang backend

giấu URL Spring

giữ cookie same-site

tránh CORS

security layer

logging

rate limit */
export async function PATCH(req: NextRequest) {
  return proxyPatchJson(req, '/cart/update-item-cart');
  // try {
  //   // 1. lấy payload từ client
  //   const body = await req.json();

  //   // 2. lấy cookie access token từ browser gửi lên
  //   const cookieHeader = req.headers.get('cookie');

  //   // 3. call sang backend thật
  //   const backendRes = await fetch(`${BACKEND_URL}/cart/update-item-cart`, {
  //     method: 'POST',
  //     headers: {
  //       'Content-Type': 'application/json',
  //       Cookie: cookieHeader || '',
  //     },
  //     body: JSON.stringify(body),
  //   });

  //   // 4. nếu backend trả lỗi
  //   if (!backendRes.ok) {
  //     const text = await backendRes.text();
  //     return NextResponse.json(
  //       { message: text || 'Update item cart failed' },
  //       { status: backendRes.status },
  //     );
  //   }

  //   // 5. parse response backend
  //   const data = await backendRes.json();

  //   // 6. trả về cho client
  //   return NextResponse.json(data);
  // } catch (error) {
  //   console.error('API /cart/update-item-cart error:', error);

  //   return NextResponse.json(
  //     { message: 'Internal server error' },
  //     { status: 500 },
  //   );
  // }
}
