// app\api\notifications\unread-count\route.ts
import { NextRequest } from 'next/server';

export const GET = async (req: NextRequest) => {
  return fetch(`${process.env.API_EXTERNAL}/notifications/unread-count`, {
    method: 'GET',
    headers: {
      cookie: req.headers.get('cookie') || '',
    },
  });
};
