'use client';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { useFormContext } from './FormContainer';
import { cn } from '@/lib/utils';
type TextAreaInputProps = {
  name: string;
  labelText?: string;
  defaultValue?: string;
  errors?: string[];
};

function TextAreaInput({ name, labelText, defaultValue }: TextAreaInputProps) {
  const state = useFormContext(); // Lấy state từ "đường ống"
  const fieldErrors = state?.errors?.[name]; // Tìm lỗi đúng theo name của input
  return (
    <div className='mb-2'>
      <Label htmlFor={name} className='capitalize'>
        {labelText || name}
      </Label>
      <Textarea
        id={name}
        name={name}
        defaultValue={defaultValue}
        rows={5}
        required
        className={cn(
          // hàm cn (thường là sự kết hợp của clsx và tailwind-merge)
          // hoặc có thể dùng toán tử giống bên FormInput component
          'leading-loose', // Class cố định luôn luôn có
          fieldErrors && 'border-red-500 focus-visible:ring-red-500', // Chỉ thêm nếu có lỗi
        )}
      />
      {fieldErrors?.map((error) => (
        <span key={error} className='text-red-500 text-sm'>
          {error}
        </span>
      ))}
    </div>
  );
}

export default TextAreaInput;
