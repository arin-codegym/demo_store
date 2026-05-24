import { NextRequest } from 'next/server';
//app\api\notifications\read-all\route.ts
export const POST = async (req: NextRequest) => {
  const body = await req.text();
  return fetch(`${process.env.API_EXTERNAL}/notifications/read-all`, {
    method: 'POST',
    headers: {
      cookie: req.headers.get('cookie') || '',
      'Content-Type': 'application/json',
    },
    body,
  });
};
