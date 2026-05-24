'use client';
import { forwardRef, useImperativeHandle, useRef, useState } from 'react';

export type ImageUploadRef = {
  clear: () => void;
};

type ImageUploadProps = {
  onUploaded: (url: string) => void;
  onError?: (message: string) => void;
};

export const ImageUpload = forwardRef<ImageUploadRef, ImageUploadProps>(
  function ImageUpload({ onUploaded, onError }, ref) {
    const [uploading, setUploading] = useState(false);
    const [preview, setPreview] = useState('');
    const inputRef = useRef<HTMLInputElement | null>(null);

    const clear = () => {
      if (inputRef.current) {
        inputRef.current.value = '';
      }
      setPreview('');
    };

    useImperativeHandle(ref, () => ({
      clear,
    }));

    const handleChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (!file) return;

      setPreview(URL.createObjectURL(file));
      setUploading(true);

      try {
        const formData = new FormData();
        formData.append('image', file);

        const res = await fetch('/api/upload-image', {
          method: 'POST',
          body: formData,
        });
        const data = await res.json();
        if (!res.ok) {
          const message =
            data?.errors?.image?.[0] || data?.message || 'Upload image failed';
          clear();
          onError?.(message); // tương đương onError && onError(message)(note:trick JS (short-circuit)); hoặc if (onError) { onError(message); }
          return;
        }
        onUploaded(data.imageLink);
      } finally {
        setUploading(false);
      }
    };

    return (
      <div className='space-y-2'>
        <input
          ref={inputRef}
          type='file'
          accept='image/*'
          onChange={handleChange}
        />
        {preview && (
          <img src={preview} alt='preview' className='w-32 rounded' />
        )}
        {uploading && <p>Uploading...</p>}
      </div>
    );
  },
);
