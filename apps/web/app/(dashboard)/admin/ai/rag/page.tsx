'use client';

import { fetchWithAuth } from '@/lib/fetchWithAuth.client';
import { useMemo, useState } from 'react';

type UploadResponse = {
  success?: boolean;
  documentId?: number;
  message?: string;
};

type RagAssistantCode = 'POLICY' | 'USER_GUIDE' | 'FAQ' | 'COMMON';

const ASSISTANT_CODES: RagAssistantCode[] = [
  'POLICY',
  'USER_GUIDE',
  'FAQ',
  'COMMON',
];

export default function RagUploadPage() {
  const [assistantCode, setAssistantCode] =
    useState<RagAssistantCode>('POLICY');
  const [file, setFile] = useState<File | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [result, setResult] = useState<UploadResponse | null>(null);
  const [error, setError] = useState<string>('');

  const fileInfo = useMemo(() => {
    if (!file) return '';
    const kb = (file.size / 1024).toFixed(2);
    return `${file.name} (${kb} KB)`;
  }, [file]);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setResult(null);

    // if (!assistantCode.trim()) {
    //   setError('assistantCode không được để trống');
    //   return;
    // }

    if (!file) {
      setError('Bạn chưa chọn file');
      return;
    }

    try {
      setIsSubmitting(true);

      const formData = new FormData();
      formData.append('assistantCode', assistantCode);
      // formData.append('operator', operator);
      formData.append('file', file);

      const response = await fetchWithAuth('/api/admin/rag/documents/import', {
        method: 'POST',
        body: formData,
      });

      const data = (await response.json()) as UploadResponse;

      if (!response.ok) {
        throw new Error(data?.message || 'Import thất bại');
      }

      setResult(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Có lỗi xảy ra khi upload');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className='min-h-screen bg-slate-50  md:p-0'>
      <div className='mx-auto max-w-2xl rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200 md:p-8'>
        <div className='mb-6'>
          <h1 className='text-2xl font-semibold text-slate-900 md:text-3xl'>
            RAG Document Upload
          </h1>
          <p className='mt-2 text-sm text-slate-600'>
            UI test import file vào <code>rag_document</code> và{' '}
            <code>rag_chunk</code>.
          </p>
        </div>

        <form onSubmit={handleSubmit} className='space-y-5'>
          <div>
            <label className='mb-2 block text-sm font-medium text-slate-700'>
              Assistant Code
            </label>
            <select
              value={assistantCode}
              onChange={(e) =>
                setAssistantCode(e.target.value as RagAssistantCode)
              }
              className='w-full rounded-2xl border border-slate-300 px-4 py-3 text-sm outline-none transition focus:border-slate-500'
            >
              {ASSISTANT_CODES.map((code) => (
                <option key={code} value={code}>
                  {code}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className='mb-2 block text-sm font-medium text-slate-700'>
              File
            </label>
            <input
              type='file'
              onChange={(e) => setFile(e.target.files?.[0] ?? null)}
              className='block w-full rounded-2xl border border-slate-300 px-4 py-3 text-sm'
            />
            {fileInfo && (
              <p className='mt-2 text-sm text-slate-500'>Đã chọn: {fileInfo}</p>
            )}
          </div>

          <button
            type='submit'
            disabled={isSubmitting}
            className='inline-flex rounded-2xl bg-slate-900 px-5 py-3 text-sm font-medium text-white transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50'
          >
            {isSubmitting ? 'Đang import...' : 'Upload và import'}
          </button>
        </form>

        {error && (
          <div className='mt-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700'>
            {error}
          </div>
        )}

        {result && (
          <div className='mt-6 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-4 text-sm text-emerald-800'>
            <p className='font-medium'>Import thành công</p>
            <p className='mt-1'>success: {String(result.success)}</p>
            <p>documentId: {result.documentId}</p>
          </div>
        )}

        <div className='mt-8 rounded-2xl bg-slate-100 p-4 text-xs text-slate-600'>
          <p className='font-semibold text-slate-700'>Lưu ý</p>
          <ul className='mt-2 list-disc space-y-1 pl-4'>
            <li>
              Backend endpoint đang gọi:{' '}
              <code>POST /api/admin/rag/documents/import</code>
            </li>
            <li>
              Nếu backend khác domain/port, cần bật CORS hoặc dùng Next.js
              proxy.
            </li>
            <li>
              Phù hợp để test flow insert <code>rag_document</code> -&gt;{' '}
              <code>rag_chunk</code>.
            </li>
          </ul>
        </div>
      </div>
    </main>
  );
}
