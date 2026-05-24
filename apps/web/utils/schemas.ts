import { z, ZodSchema } from 'zod';

export const imageSchema = z.object({
  image: validateImageFile(), //tách thành fuction không thích thì dung arrow func
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
    //cách làm theo dạng arrow func có thể refine nhiều func mỗi refine tương đương một func xử lý logic
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
  //  image: z.string().optional().or(z.literal('')), chấp nhận không ảnh
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

// export type CreateAndEditJobType = z.infer<typeof createProductSchema>;
// export function validateWithZodSchema<T>( cách làm của tutorial basic
//   schema: ZodSchema<T>,
//   data: unknown,
// ): T {
//   const result = schema.safeParse(data);
//   if (!result.success) {
//     const errors = result.error.errors.map((error) => error.message);
//     throw new Error(errors.join(', '));
//   }
//   return result.data;
// }

// export function validateWithZodSchema<T>(
//   schema: ZodSchema<T>,
//   data: any
// ): T {
//   const result = schema.safeParse(data);

//   if (!result.success) {
//     const errors = result.error.flatten().fieldErrors;

//     // Ở đây chúng ta throw một object chứa cả message và errors
//     // Để hàm createProductAction có thể bắt được trong khối catch
//     // dùng trực tiếp đơn giản nhưng muốn common bài bản
// thì tạo class cho chuyên nghiệp tức định nghĩa kiểu dữ liệu rõ ràng dang generic
//     throw {
//       message: 'Validation failed',
//       errors: errors,
//     };
//   }

//   return result.data;
// }

export function validateWithZodSchema<T>(schema: ZodSchema<T>, data: any): T {
  const result = schema.safeParse(data);

  // 2. Trong hàm validateWithZodSchema
  if (!result.success) {
    // console.log(result.error.flatten());
    // console.log(result.error.format());
    // console.log(result.error.issues);
    throw new ValidationError(
      'Validation failed',
      //fieldErrors: { image: [ 'File size must be less than 1 MB' ] }
      result.error.flatten().fieldErrors,
    );
  }

  return result.data;
}
// 1. Tạo lớp lỗi riêng
export class ValidationError extends Error {
  // Thêm undefined vào đây để khớp với Zod

  errors: Record<string, string[] | undefined>;

  constructor(message: string, errors: Record<string, string[] | undefined>) {
    super(message);
    this.name = 'ValidationError';
    this.errors = errors;
  }
}
