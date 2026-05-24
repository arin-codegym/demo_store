//app\api\notifications\route.ts
import { NextRequest } from 'next/server';

export const GET = async (req: NextRequest) => {
  const searchParams = req.nextUrl.searchParams;
  return fetch(`${process.env.API_EXTERNAL}/notifications?${searchParams}`, {
    method: 'GET',
    headers: {
      cookie: req.headers.get('cookie') || '',
    },
  });
};
