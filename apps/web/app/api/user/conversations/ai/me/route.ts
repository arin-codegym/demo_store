import { NextRequest, NextResponse } from 'next/server';

export const GET = async (req: NextRequest) => {
  // 1. Tạo một bản sao của headers từ request gốc
  // Chúng ta dùng new Headers(req.headers) để có một instance sạch
  const forwardedHeaders = new Headers(req.headers);
  // 2. QUAN TRỌNG: Phải xóa hoặc ghi đè header 'host'
  // Nếu bê nguyên 'host' của localhost/frontend sang Backend,
  // Backend có thể chặn request vì sai domain.
  forwardedHeaders.delete('host');
  const { searchParams } = new URL(req.url);
  const assistantCode = searchParams.get('assistantCode');
  console.log(searchParams);
  console.log(assistantCode);
  const res = await fetch(
    `${process.env.API_EXTERNAL}/conversations/ai/me?assistantCode=${assistantCode}`,
    {
      method: 'GET',
      headers: req.headers,
    },
  );
  return res;
};
