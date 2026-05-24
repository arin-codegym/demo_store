import { cookies } from 'next/headers';
import { NextRequest } from 'next/server';

export const GET = async (req: NextRequest) => {
  const cookieHeader = req.headers.get('cookie') ?? '';
  const searchParams = req.nextUrl.searchParams;
  const pathname = new URL(req.url).pathname.substring(9);
  return fetch(
    `${process.env.API_EXTERNAL}${pathname}?keyword=${searchParams.get('keyword')}`,
    {
      method: 'GET',
      headers: {
        cookie: cookieHeader,
      },
    },
  );
};
