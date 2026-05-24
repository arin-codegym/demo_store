import { NextRequest } from 'next/server';

export const POST = async (req: NextRequest) => {
  const body = await req.json();
  return fetch(`${process.env.API_EXTERNAL}/messages`, {
    method: 'post',
    headers: req.headers,
    body: JSON.stringify(body),
  });
};
