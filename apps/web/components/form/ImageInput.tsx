'use client';
import { Label } from '../ui/label';
import { Input } from '../ui/input';
import { useFormContext } from './FormContainer';

function ImageInput() {
  const name = 'image';
  const state = useFormContext(); // Lấy state từ "đường ống"
  const fieldErrors = state?.errors?.[name]; // Tìm lỗi đúng theo name của input
  return (
    <div className='mb-2'>
      <Label htmlFor={name} className='capitalize'>
        Image
      </Label>
      <Input id={name} name={name} type='file' required accept='image/*' />
      {fieldErrors?.map((error) => (
        <span key={error} className='text-red-500 text-sm'>
          {error}
        </span>
      ))}
    </div>
  );
}
export default ImageInput;
