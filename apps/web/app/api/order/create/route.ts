import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.API_EXTERNAL;

export async function POST(req: NextRequest) {
  try {
    // 1. lấy payload từ client
    // const body = await req.json();
    // 2. lấy cookie access token từ browser gửi lên
    const cookieHeader = req.headers.get('cookie');
    const key = req.headers.get('idempotency-key');
    // 3. call sang backend thật
    const backendRes = await fetch(`${BACKEND_URL}/order/create`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Idempotency-Key': key || '',
        Cookie: cookieHeader || '',
      },
      // body: JSON.stringify(body),
    });
    const text = await backendRes.text();
    // 4. nếu backend trả lỗi
    if (!backendRes.ok) {
      return NextResponse.json(
        {
          message: text || 'Create order failed',
          backendStatus: backendRes.status,
        },
        { status: backendRes.status },
      );
    }

    let data: any;
    try {
      data = JSON.parse(text);
    } catch {
      data = { text };
    }

    return NextResponse.json(data);
  } catch (error) {
    console.error('API /order/create error:', error);

    return NextResponse.json(
      { message: 'Internal server error' },
      { status: 500 },
    );
  }
}
