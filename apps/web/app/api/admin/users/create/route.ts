//app\api\admin\users\create\route.ts
import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  try {
    // const pathname = new URL(req.url).pathname;
    const cookieHeader = req.headers.get('cookie') ?? '';
    const payload = await req.json();
    // payload null => request body không phải JSON
    if (payload == null) {
      return NextResponse.json(
        { message: 'Invalid JSON body' },
        { status: 400 },
      );
    }
    const upstream = await fetch(
      `${process.env.API_EXTERNAL}/admin/users/create`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          cookie: cookieHeader,
          accept: 'application/json',
        },
        body: JSON.stringify(payload),
      },
    );
    let data: any = null;
    if (upstream.status === 204) {
      // 204: tuyệt đối không trả body
      return NextResponse.json({ success: true }, { status: 200 });
    }
    if (upstream.status === 409) {
      const err = await upstream.json(); // "Username đã tồn tại"
      return NextResponse.json(err, { status: 409 });
    }

    const ct = upstream.headers.get('content-type') ?? '';

    if (ct.includes('application/json')) {
      data = await upstream.json().catch(() => null);
    } else {
      data = await upstream.text().catch(() => '');
    }

    return NextResponse.json(data, { status: upstream.status });
  } catch (err) {
    // lỗi mạng / fetch throw / code throw
    return NextResponse.json(
      {
        message: 'Proxy error',
        detail: err instanceof Error ? err.message : String(err),
      },
      { status: 502 },
    );
  }
}
