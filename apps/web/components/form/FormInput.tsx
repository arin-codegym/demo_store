'use client';
import { Label } from '../ui/label';
import { Input } from '../ui/input';
import { useFormContext } from './FormContainer';
// import { FormContext } from './FormContainer'; // Import cái "đường ống"
type FormInputProps = {
  name: string;
  type: string;
  label?: string;
  defaultValue?: string;
  placeholder?: string;
  errors?: string[];
};

function FormInput({
  label,
  name,
  type,
  defaultValue,
  placeholder,
  errors, //"truyền thủ công" (Prop Drilling) và "tự động hóa" (Encapsulation). hiện tại dùng Encapsulation
}: FormInputProps) {
  // Lấy dữ liệu trực tiếp bằng useContext của React
  // const state = useContext(FormContext);

  const state = useFormContext(); // Lấy state từ "đường ống"
  const fieldErrors = state?.errors?.[name]; // Tìm lỗi đúng theo name của input
  return (
    <div className='mb-2'>
      <Label htmlFor={name} className='capitalize'>
        {label || name}
      </Label>
      <Input
        id={name}
        name={name}
        type={type}
        defaultValue={defaultValue}
        placeholder={placeholder}
        className={
          fieldErrors ? 'border-red-500 focus-visible:ring-red-500' : ''
        }
        required
      />
      {fieldErrors?.map((error) => (
        <span key={error} className='text-red-500 text-sm'>
          {error}
        </span>
      ))}
    </div>
  );
}

export default FormInput;
