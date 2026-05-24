import { NextRequest } from 'next/server';

export async function DELETE(
  req: NextRequest,
  context: { params: Promise<{ id: string }> },
) {
  const { id } = await context.params;
  const cookieHeader = req.headers.get('cookie') ?? '';
  return fetch(`${process.env.API_EXTERNAL}/favorites/${id}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      cookie: cookieHeader,
      accept: 'application/json',
    },
  });
}
