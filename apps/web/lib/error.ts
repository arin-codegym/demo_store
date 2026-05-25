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

type FormResponse = {
  message: string;
  errors?: Record<string, string[] | undefined>;
};

export const renderError = (error: unknown): FormResponse => {
  // Zod validation errors carry field-level errors for form components.
  if (error instanceof ValidationError) {
    return {
      message: error.message,
      errors: error.errors,
    };
  }

  if (error instanceof Error) {
    return {
      message: error.message,
      errors: {},
    };
  }

  return {
    message: 'An unexpected error occurred',
    errors: {},
  };
};
