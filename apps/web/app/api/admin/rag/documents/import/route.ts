import { NextRequest, NextResponse } from 'next/server';

export const POST = async (req: NextRequest) => {
  const formData = await req.formData();
  return fetch(`${process.env.API_EXTERNAL}/admin/rag/documents/import`, {
    method: 'POST',
    headers: {
      // Truyền tiếp token từ Client -> Next.js -> Spring Boot
      cookie: req.headers.get('cookie') || '',
    },
    body: formData, // Cứ vứt formData vào, Next.js fetch sẽ tự lo phần Content-Type và boundary
  });
};
