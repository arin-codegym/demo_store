export async function POST(req: Request) {
  const body = await req.json();

  const res = await fetch(`${process.env.API_EXTERNAL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    // Đọc body lỗi từ backend trả về (để lấy chữ "Tài khoản bị khóa")
    const errorData = await res.json().catch(() => ({}));
    // Trả về kèm theo đúng mã status của backend (res.status sẽ là 403)
    return Response.json(errorData, { status: res.status });
  }
  const respose = Response.json({ ok: true });
  for (const cookie of res.headers.getSetCookie()) {
    respose.headers.append('set-cookie', cookie);
  }

  return respose;
}
