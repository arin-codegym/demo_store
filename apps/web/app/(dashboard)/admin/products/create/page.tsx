'use client';
// import FormInput from '@/components/form/FormInput';
// import { SubmitButton } from '@/components/form/Buttons';
// import FormContainer from '@/components/form/FormContainer';
// import ImageInput from '@/components/form/ImageInput';
// import PriceInput from '@/components/form/PriceInput';
// import TextAreaInput from '@/components/form/TextAreaInput';
import { faker } from '@faker-js/faker';
// import CheckboxInput from '@/components/form/CheckboxInput';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createProductSchema } from '@/utils/schemas';
import {
  CustomCheckboxField,
  CustomFormField,
  CustomTextAreaField,
} from '@/components/form/CustomForm';
import { Button } from '@/components/ui/button';
import useCreateProduct from '@/query/product/useCreateProduct';
import z from 'zod';
import { ImageUpload, ImageUploadRef } from '@/components/form/ImageUpload';
import { useRef } from 'react';
import { toast } from '@/components/ui/use-toast';

type CreateProduct = z.infer<typeof createProductSchema>;
type CreateProductPayload = {
  name: string;
  company: string;
  price: number;
  description: string;
  featured: boolean;
  image?: string;
};
function CreateProduct() {
  const createProductMutation = useCreateProduct();
  const imageUploadRef = useRef<ImageUploadRef | null>(null);
  const name = faker.commerce.productName();
  const company = faker.company.name();
  // const description = faker.commerce.productDescription();
  const description = faker.lorem.paragraph({ min: 10, max: 12 });
  // 1. Define your form.
  const form = useForm<CreateProduct>({
    resolver: zodResolver(createProductSchema),
    defaultValues: {
      name: name,
      company: company,
      price: 100,
      image: '',
      description: description,
      featured: false,
    },
  });
  const onSubmit = async (values: CreateProduct) => {
    try {
      const payload: CreateProductPayload = {
        ...values,
      };
      await createProductMutation.mutateAsync(payload);
      toast({ description: 'Create product successfully' });
      form.reset();
      imageUploadRef.current?.clear();
    } catch (e: any) {
      await fetch('/api/admin/product/create', {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ imageUrl: values.image }),
      });
      toast({
        title: 'Create failed',
        description: e?.message ?? 'Something went wrong',
        variant: 'destructive',
      });
    }

    // form.setValue('image', '', { shouldValidate: true });
    // imageUploadRef.current?.clear();
  };
  const onError = (errors: any) => {
    console.log('submit errors', errors);
  };
  return (
    <section>
      <h1 className='text-2xl font-semibold mb-8 capitalize'>create product</h1>
      <div className='border p-8 rounded-md'>
        {/* <FormContainer action={createProductAction}>
          <div className='grid gap-4 md:grid-cols-2 my-4'>
            <FormInput
              type='text'
              name='name'
              label='product name'
              defaultValue={name}
            />
            <FormInput
              type='text'
              name='company'
              label='company'
              defaultValue={company}
            />
            <PriceInput />
            <ImageInput />
          </div>
          <TextAreaInput
            name='description'
            labelText='product description'
            defaultValue={description}
          />
          <div className='mt-6'>
            <CheckboxInput name='featured' label='featured' />
          </div>

          <SubmitButton text='Create Product' className='mt-8' />
        </FormContainer> */}
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit, onError)}>
            <CustomFormField name='name' control={form.control} />
            <CustomFormField name='company' control={form.control} />
            <CustomFormField
              name='price'
              control={form.control}
              label='Price ($)'
              type='number'
              min={0}
              required
            />
            {/* <CustomFormField
              name='image'
              control={form.control}
              label='Image'
              type='file'
              accept='image/*'
              required
            /> */}
            {/* <ImageUpload
              ref={imageUploadRef}
              onUploaded={(fullPath) => {
                form.setValue('image', fullPath, { shouldValidate: true });
              }}
            /> */}
            <FormField
              control={form.control}
              name='image'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Image</FormLabel>
                  <FormControl>
                    <ImageUpload
                      ref={imageUploadRef}
                      onUploaded={(fullPath) => {
                        form.clearErrors('image');
                        field.onChange(fullPath);
                      }}
                      onError={(message) => {
                        form.setError('image', {
                          type: 'server',
                          message,
                        });
                      }}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <CustomTextAreaField
              name='description'
              control={form.control}
              label='Description'
              rows={5}
              required
            />
            <CustomCheckboxField
              name='featured'
              control={form.control}
              label='featured product'
            />
            <div className='flex items-center justify-end gap-2'>
              <Button
                type='button'
                variant='outline'
                onClick={() => {
                  form.reset();
                  imageUploadRef.current?.clear();
                }}
              >
                Reset
              </Button>
              <Button type='submit' disabled={createProductMutation.isPending}>
                {createProductMutation.isPending ? 'Creating...' : 'Create'}
              </Button>
            </div>
          </form>
        </Form>
      </div>
    </section>
  );
}
export default CreateProduct;
