import { NextResponse } from 'next/server';
export async function POST(req: Request) {
  try {
    const cookie = req.headers.get('cookie');

    const body = await req.json();

    const response = await fetch(
      `${process.env.API_EXTERNAL}/payment/create-session`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Cookie: cookie || '',
        },
        body: JSON.stringify({
          orderId: body.orderId,
          cartId: body.cartId,
        }),
      },
    );
    const data = await response.json();

    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json(
      { message: 'Error creating payment session' },
      { status: 500 },
    );
  }
}
