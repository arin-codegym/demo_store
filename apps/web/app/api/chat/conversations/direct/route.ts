import { NextRequest, NextResponse } from 'next/server';

export const POST = async (req: NextRequest) => {
  // 1. Dùng await req.json() để đọc stream và parse thành Javascript Object
  const body = await req.json();
  try {
    return fetch(`${process.env.API_EXTERNAL}/conversations/direct`, {
      method: 'POST',
      headers: {
        'content-type': 'application/json',
        cookie: req.headers.get('cookie') || '',
      },
      body: JSON.stringify(body),
    });
  } catch (error) {
    console.error(error);
    return NextResponse.json(
      { error: 'Failed to create direct conversation' },
      { status: 500 },
    );
  }
};
