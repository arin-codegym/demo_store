export async function loginClient(username: string, password: string) {
  const res = await fetch(`api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
    credentials: 'include',
  });

  // Login errors may come back as JSON or plain text depending on backend path.
  let payload: any = null;
  const contentType = res.headers.get('content-type') || '';
  try {
    if (contentType.includes('application/json')) {
      payload = await res.json();
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
      payload?.error ||
      'Sai tài khoản hoặc mật khẩu.';
    throw new Error(msg);
  }

  return res.json().catch(() => null);
}
