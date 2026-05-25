import { z, ZodSchema } from 'zod';

export const imageSchema = z.object({
  image: validateImageFile(),
});

function validateImageFile() {
  const maxUploadSize = 5 * 1024 * 1024;
  const acceptedFileTypes = ['image/'];
  return z
    .instanceof(File)
    .refine((file) => {
      return !file || file.size <= maxUploadSize;
    }, `File size must be less than 1 MB`)
    .refine((file) => {
      return (
        !file || acceptedFileTypes.some((type) => file.type.startsWith(type))
      );
    }, 'File must be an image');
}
export const productSchemaBase = z.object({
  name: z
    .string()
    .min(2, {
      message: 'Name must be at least 2 characters.',
    })
    .max(100, {
      message: 'Name must be less than 100 characters.',
    }),
  company: z.string(),
  featured: z.coerce.boolean(),
  price: z.coerce.number().int().min(0, {
    message: 'Price must be a positive number.',
  }),
  // imageUrl: z.string().optional().or(z.literal('')),
  // image: z.instanceof(File).refine((file) => { return !file || file.size <= maxUploadSize; }, `File size must be less than 1 MB`),
  // image: validateImageFile(),
  description: z.string().refine(
    (description) => {
      const wordCount = description.split(' ').length;
      return wordCount >= 10 && wordCount <= 1000;
    },
    {
      message: 'Description must be between 10 and 1000 words.',
    },
  ),
});
export const createProductSchema = productSchemaBase.extend({
  image: z.string().min(1, 'Image is required'),
});

export const editProductSchema = productSchemaBase.extend({
  image: z.string().optional().or(z.literal('')),
});

export const reviewSchema = z.object({
  productId: z.string().refine((value) => value !== '', {
    message: 'Product ID cannot be empty',
  }),
  authorName: z.string().refine((value) => value !== '', {
    message: 'Author name cannot be empty',
  }),
  authorImageUrl: z.string().refine((value) => value !== '', {
    message: 'Author image URL cannot be empty',
  }),
  rating: z.coerce
    .number()
    .int()
    .min(1, { message: 'Rating must be at least 1' })
    .max(5, { message: 'Rating must be at most 5' }),
  comment: z
    .string()
    .min(10, { message: 'Comment must be at least 10 characters long' })
    .max(1000, { message: 'Comment must be at most 1000 characters long' }),
});

export function validateWithZodSchema<T>(schema: ZodSchema<T>, data: any): T {
  const result = schema.safeParse(data);

  if (!result.success) {
    // Preserve field-level errors so form inputs can render the right message.
    throw new ValidationError(
      'Validation failed',
      result.error.flatten().fieldErrors,
    );
  }

  return result.data;
}

export class ValidationError extends Error {
  errors: Record<string, string[] | undefined>;

  constructor(message: string, errors: Record<string, string[] | undefined>) {
    super(message);
    this.name = 'ValidationError';
    this.errors = errors;
  }
}
