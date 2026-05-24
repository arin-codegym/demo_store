export async function loginClient(username: string, password: string) {
  const res = await fetch(`api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
    credentials: 'include', // ✅ cực quan trọng
    // mode: 'cors',                 // (thường không cần, nhưng có thể thêm)
  });
  // ✅ luôn thử đọc body (json hoặc text) để lấy message
  let payload: any = null;
  const contentType = res.headers.get('content-type') || '';
  try {
    if (contentType.includes('application/json')) {
      payload = await res.json();
      // console.log(payload);
    } else {
      const text = await res.text();
      payload = text ? JSON.parse(text) : null;
    }
  } catch {
    // ignore parse errors
  }

  if (!res.ok) {
    const msg =
      payload?.message ||
      payload?.error || // phòng khi backend trả field khác
      'Sai tài khoản hoặc mật khẩu.';
    throw new Error(msg);
  }

  // backend bạn đang trả LoginResponse(userDto) => có thể lấy nếu cần
  return res.json().catch(() => null);
}
