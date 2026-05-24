import { ValidationError } from '@/utils/schemas';

export class ApiError extends Error {
  status: number;
  data: unknown;

  constructor(message: string, status: number, data?: unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

export function pickMessage(body: any): string | null {
  if (!body) return null;
  if (typeof body === 'string') return body;
  if (typeof body.message === 'string') return body.message;
  if (typeof body.error === 'string') return body.error;

  if (body.errors && typeof body.errors === 'object') {
    return Object.entries(body.errors)
      .map(([k, v]) => `${k}: ${String(v)}`)
      .join('\n');
  }

  if (typeof body === 'object') {
    const pairs = Object.entries(body)
      .filter(([, v]) => typeof v === 'string')
      .map(([k, v]) => `${k}: ${v}`);
    if (pairs.length) return pairs.join('\n');
  }

  return null;
}

export async function readBody(
  res: Response,
): Promise<{ data: unknown; message?: string }> {
  const ct = res.headers.get('content-type') || '';
  try {
    if (ct.includes('application/json')) {
      const data = await res.json();
      return { data, message: pickMessage(data) ?? undefined };
    }
    const text = await res.text();
    return { data: text, message: text || undefined };
  } catch {
    return { data: null };
  }
}

// /* Validation */
// // 1. Định nghĩa một kiểu dữ liệu chung
type FormResponse = {
  message: string;
  errors?: Record<string, string[] | undefined>; // Ví dụ: { name: ['Quá ngắn'], price: ['Phải là số'] }
  // errors?: {}; // Có dấu '?' để nhánh thành công không bắt buộc phải có
};

export const renderError = (error: unknown): FormResponse => {
  console.log(error);
  // Nếu là lỗi từ Zod (nếu bạn quăng lỗi zod ở đâu đó) cách này làm đơn giản nếu không dùng custom class validator
  // if (error instanceof Error && 'errors' in error) {
  //   return {
  //     message: 'Validation failed',
  //     errors: (error as any).errors.map((e: any) => e.message),
  //   };
  // }

  // return {
  //   message: error instanceof Error ? error.message : 'an error occurred',
  //   errors: {}, // Trả về object rỗng nếu không có lỗi field cụ thể
  // };
  // 1. Nếu là lỗi Validation do mình chủ động throw
  if (error instanceof ValidationError) {
    return {
      message: error.message,
      errors: error.errors, // Kiểu dữ liệu đã khớp: Record<string, string[] | undefined>
    };
  }
  // Xử lý các lỗi Error thông thường khác...
  // return { message: 'An error occurred', errors: {} };
  // 2. Nếu là lỗi Error thông thường (ví dụ: throw new Error("Database connection failed"))
  if (error instanceof Error) {
    return {
      message: error.message,
      errors: {}, // Trả về object rỗng để không gây lỗi ở FormInput
    };
  }

  // 3. Trường hợp fallback cuối cùng (lỗi không xác định)
  return {
    message: 'An unexpected error occurred',
    errors: {},
  };
};
