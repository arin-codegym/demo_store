'use server';
import { LoginState } from '@/app/(auth)/login/page';
import { cookies, headers } from 'next/headers';
import { useMutation, useQueryClient } from '@tanstack/react-query';
const initialState: LoginState = {
  error: null,
  success: false,
  username: '',
};

export const loginAction = async (
  prevState: LoginState,
  formData: FormData,
): Promise<LoginState> => {
  const username = formData.get('username');
  const password = formData.get('password');

  try {
    const apiUrl = process.env.API_EXTERNAL;
    if (!apiUrl) {
      return { ...initialState, error: 'Chưa cấu hình API URL.' };
    }

    const baseUrl = apiUrl.endsWith('/') ? apiUrl.slice(0, -1) : apiUrl;
    const fullUrl = `${baseUrl}/auth/login`;

    const response = await fetch(fullUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
      cache: 'no-store',
    });

    if (!response.ok) {
      const data = await response.json();
      return {
        ...initialState,
        error: data.error || 'Sai tài khoản hoặc mật khẩu.',
        username: username as string,
      };
    }

    // ✅ Chỉ set refreshToken
    const cookiesAllInHeader = response.headers.getSetCookie();
    if (cookiesAllInHeader) {
      const refreshToken = cookiesAllInHeader.find((cookie) =>
        cookie.includes('refreshToken='),
      );
      const accessToken = cookiesAllInHeader.find((cookie) =>
        cookie.includes('accessToken='),
      );

      if (refreshToken && accessToken) {
        const cookieStore = await cookies();

        cookieStore.set(
          'refreshToken',
          refreshToken.split(';')[0].split('=')[1],
          {
            httpOnly: true,
            sameSite: 'lax',
            secure: process.env.NODE_ENV === 'production',
            path: '/',
            maxAge: refreshToken.split(';')[2].includes('Max-Age=')
              ? parseInt(refreshToken.split(';')[2].split('=')[1])
              : 7 * 24 * 60 * 60,
          },
        );
        cookieStore.set(
          'accessToken',
          accessToken.split(';')[0].split('=')[1],
          {
            httpOnly: true,
            sameSite: 'lax',
            secure: process.env.NODE_ENV === 'production',
            path: '/',
            maxAge: accessToken.split(';')[2].includes('Max-Age=')
              ? parseInt(accessToken.split(';')[2].split('=')[1])
              : 60 * 60,
          },
        );
      }
    }

    // ❌ KHÔNG trả user, KHÔNG token
    return {
      success: true,
      error: null,
      username: prevState.username, // Giữ nguyên username đã nhập để tránh mất khi có lỗi
    };
  } catch (e) {
    return {
      ...initialState,
      error: 'Không thể kết nối Backend.',
      username: username as string,
    };
  }
};

export async function useLogin() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data) => {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(data),
        credentials: 'include',
      });

      if (!res.ok) throw new Error();

      return res.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['me'] });
    },
  });
}
/**
 * HÀM QUAN TRỌNG: Lấy hoặc Làm mới Token
 * Hàm này sẽ được gọi ở đầu mỗi Server Action (như addToCart)
 */
export const getValidToken = async () => {
  // 1. Khởi tạo Store (Bắt buộc await trên Next.js 15+)
  const cookieStore = await cookies();
  const headerStore = await headers();

  // 2. Lấy Access Token hiện tại
  let accessToken = cookieStore.get('accessToken')?.value;

  // Nếu có Access Token, ta trả về luôn (tạm thời chưa check User ở bước này để tối ưu)
  if (accessToken) {
    return { token: accessToken, user: null };
  }

  // 3. Nếu mất Access Token, kiểm tra Refresh Token
  let refreshToken = cookieStore.get('refreshToken')?.value;
  // Log ra để kiểm tra tất cả cookie mà Server nhận được
  const allCookies = cookieStore.getAll();
  // console.log('Tất cả cookies nhận được tại Server:', allCookies);

  // --- CƠ CHẾ DỰ PHÒNG (Bóc tách thủ công từ Header) ---
  // Đôi khi trên Turbopack, cookieStore.get() bị rỗng dù trình duyệt có gửi.
  if (!refreshToken) {
    const rawCookie = headerStore.get('cookie');
    if (rawCookie && rawCookie.includes('refreshToken=')) {
      const match = rawCookie.match(/refreshToken=([^;]+)/);
      refreshToken = match ? match[1] : undefined;
    }
  }

  // 2. Nếu mất accessToken, kiểm tra refreshToken
  if (!refreshToken) return null; // Bắt buộc phải login lại

  // 3. Tiến hành gọi Spring Boot để đổi Token mới (Silent Refresh)
  try {
    const apiProxyUrl = process.env.NEXT_PUBLIC_API_URL_PROXY;
    /**
     * SỬA LỖI ECONNRESET:
     * - Thêm body rỗng {} nếu backend yêu cầu Content-Type application/json
     * - Một số server sẽ đóng kết nối nếu thấy Content-Type mà body lại trống.
     */
    /**
     * GIẢI PHÁP: Lấy toàn bộ headers của request hiện tại (bao gồm cả Cookie)
     * và chuyển tiếp chúng sang Proxy.
     */
    const clientHeaders = await headers();
    const response = await fetch(`${apiProxyUrl}/auth/refresh`, {
      method: 'GET',
      headers: {
        // Chuyển tiếp "túi hành lý" chứa Cookie sang cho Proxy
        Cookie: clientHeaders.get('cookie') || '',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({}),
      cache: 'no-store',
    });

    if (!response.ok) throw new Error('Refresh token expired');
    if (response.ok) {
      const data = await response.json();
      const newAccessToken = data.accessToken;

      if (newAccessToken) {
        /**
         * GIẢI PHÁP "HYBRID" ĐỂ TRÁNH LỖI 500:
         * Chúng ta bọc lệnh set() trong try-catch.
         * - Nếu đang trong Action: Lệnh chạy thành công, cookie được cập nhật.
         * - Nếu đang trong Page: Next.js sẽ báo lỗi, catch sẽ bắt lấy.
         * Token vẫn được trả về để Page render tiếp, nhưng cookie sẽ không được lưu lúc này.
         * (Việc lưu cookie bền vững sẽ do Proxy xử lý khi gọi API).
         */
        try {
          cookieStore.set('accessToken', newAccessToken, {
            httpOnly: true,
            secure: process.env.NODE_ENV === 'production',
            sameSite: 'lax',
            path: '/',
            maxAge: 60 * 60,
          });
          console.log(
            '>>> [AUTH]: Đã cập nhật Cookie thành công (Action context).',
          );
        } catch (e) {
          // Rơi vào đây khi gọi hàm này trong lúc đang Render Page
          console.warn(
            '>>> [AUTH]: Đang trong bối cảnh Render, bỏ qua việc ghi Cookie.',
          );
        }

        return newAccessToken;
      }
    }

    return null;
  } catch (error) {
    console.error('>>> [REFRESH ERROR]:', error);
    return null;
  }
};

export const logoutAction = async () => {
  const cookieStore = await cookies();
  cookieStore.delete('accessToken');
  cookieStore.delete('refreshToken');
  cookieStore.delete('sid');

  return { success: true };
};

export async function readAccessToken() {
  const cookieStore = await cookies();
  return cookieStore.get('accessToken')?.value ?? null;
}
export async function useLogout() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () =>
      fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include',
      }),
    onSuccess: () => {
      queryClient.clear();
    },
  });
}
