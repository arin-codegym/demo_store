import { Control } from 'react-hook-form';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '../ui/textarea';
import { cn } from '@/lib/utils';
import { Checkbox } from '@/components/ui/checkbox';

type CustomFormFieldProps = {
  name: string;
  control: any;
  label?: string;
  type?: string;
  accept?: string;
  min?: number;
  required?: boolean;
  placeholder?: string;
};

export function CustomFormField({
  name,
  control,
  label,
  type = 'text',
  accept,
  min,
  required,
  placeholder,
}: CustomFormFieldProps) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem>
          <FormLabel className='capitalize'>{label || name}</FormLabel>
          <FormControl>
            {type === 'file' ? (
              <Input
                id={name}
                name={name}
                type='file'
                accept={accept}
                required={required}
                onChange={(e) => field.onChange(e.target.files?.[0] || null)}
              />
            ) : (
              <Input
                {...field}
                id={name}
                type={type}
                min={min}
                required={required}
                placeholder={placeholder}
              />
            )}
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

type CustomFileFieldProps = {
  name: string;
  control: any;
  label?: string;
  accept?: string;
  required?: boolean;
};

export function CustomFileField({
  name,
  control,
  label,
  accept,
  required,
}: CustomFileFieldProps) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem>
          <FormLabel className='capitalize'>{label || name}</FormLabel>
          <FormControl>
            <Input
              id={name}
              name={name}
              type='file'
              accept={accept}
              required={required}
              onChange={(e) => field.onChange(e.target.files?.[0] || null)}
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

type CustomFormSelectProps = {
  name: string;
  control: Control<any>;
  items: string[];
  labelText?: string;
};

export function CustomFormSelect({
  name,
  control,
  items,
  labelText,
}: CustomFormSelectProps) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem>
          <FormLabel className='capitalize'>{labelText || name}</FormLabel>
          <Select onValueChange={field.onChange} defaultValue={field.value}>
            <FormControl>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              {items.map((item) => {
                return (
                  <SelectItem key={item} value={item}>
                    {item}
                  </SelectItem>
                );
              })}
            </SelectContent>
          </Select>

          <FormMessage />
        </FormItem>
      )}
    />
  );
}
export default CustomFormSelect;

type CustomTextAreaFieldProps = {
  name: string;
  control: any;
  label?: string;
  placeholder?: string;
  rows?: number;
  required?: boolean;
  defaultValue?: string;
};

export function CustomTextAreaField({
  name,
  control,
  label,
  placeholder,
  rows = 5,
  required = true,
}: CustomTextAreaFieldProps) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field, fieldState }) => (
        <FormItem>
          <FormLabel className='capitalize'>{label || name}</FormLabel>
          <FormControl>
            <Textarea
              {...field}
              id={name}
              placeholder={placeholder}
              rows={rows}
              required={required}
              className={cn(
                'leading-loose',
                fieldState.error && 'border-red-500 focus-visible:ring-red-500',
              )}
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

type CustomCheckboxFieldProps = {
  name: string;
  control: any;
  label: string;
};

export function CustomCheckboxField({
  name,
  control,
  label,
}: CustomCheckboxFieldProps) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem className='flex flex-row items-center space-x-2 space-y-0'>
          <FormControl>
            <Checkbox
              id={name}
              checked={!!field.value}
              onCheckedChange={field.onChange}
            />
          </FormControl>
          <FormLabel htmlFor={name} className='text-sm leading-none capitalize'>
            {label}
          </FormLabel>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}
