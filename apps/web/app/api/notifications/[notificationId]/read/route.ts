import { NextRequest } from 'next/server';
//app\api\notifications\[notificationId]\read\route.ts
export const POST = async (
  req: NextRequest,
  context: { params: Promise<{ notificationId: string }> },
) => {
  const temp = await context.params;
  const notificationId = temp.notificationId;
  return fetch(
    `${process.env.API_EXTERNAL}/notifications/${notificationId}/read`,
    {
      method: 'POST',
      headers: {
        cookie: req.headers.get('cookie') || '',
      },
    },
  );
};
