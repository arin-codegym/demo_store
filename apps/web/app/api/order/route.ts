///api/orders API endpoit next
import { NextRequest, NextResponse } from 'next/server';
const BACKEND_URL = process.env.API_EXTERNAL;
export async function GET(req: NextRequest) {
  try {
    const backendRes = await fetch(`${BACKEND_URL}/orders/is-paid`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        Cookie: req.headers.get('cookie') || '',
      },
    });

    // 4. nếu backend trả lỗi
    if (!backendRes.ok) {
      const text = await backendRes.text();
      return NextResponse.json(
        { message: text || 'Failed to fetch cart details' },
        { status: backendRes.status },
      );
    }

    // 5. parse response backend
    const data = await backendRes.json();

    // 6. trả về cho client
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json(
      { message: 'Internal server error' },
      { status: 500 },
    );
  }
}
