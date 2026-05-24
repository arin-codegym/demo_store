import { Label } from '../ui/label';
import { Input } from '../ui/input';

const name = 'price';
type FormInputNumberProps = {
  defaultValue?: number;
  errors?: string[];
};

function PriceInput({ defaultValue, errors }: FormInputNumberProps) {
  return (
    <div className='mb-2'>
      <Label htmlFor='price' className='capitalize'>
        Price ($)
      </Label>
      <Input
        id={name}
        type='number'
        name={name}
        min={0}
        defaultValue={defaultValue || 100}
        required
      />
      {errors?.map((error) => (
        <span key={error} className='text-red-500 text-sm'>
          {error}
        </span>
      ))}
    </div>
  );
}
export default PriceInput;
