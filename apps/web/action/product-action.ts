'use server';
import { renderError } from '@/lib/error';
import {
  imageSchema,
  productSchemaBase,
  validateWithZodSchema,
} from '@/utils/schemas';
import { deleteImage, uploadImage } from '@/utils/s3';
import { Product } from '@/utils/types';
import { revalidatePath, revalidateTag } from 'next/cache';
import { headers } from 'next/headers';
import { redirect } from 'next/navigation';

export const fetchFeaturedProducts = async () => {
  const res = await fetch(
    `${process.env.API_EXTERNAL}/product/featuredProducts`,
    {
      method: 'GET',
      cache: 'no-store',
    },
  );
  if (!res.ok) {
    throw new Error('Failed to fetch featured products');
  }

  const data = await res.json();
  return data.featureProducts ?? [];
};

export const fetchAllProducts = async (): Promise<Product[]> => {
  const res = await fetch(
    `${process.env.API_EXTERNAL}/product/fetchAllProducts`,
    {
      method: 'GET',
      cache: 'force-cache',
      next: {
        tags: ['products'],
      },
    },
  );
  if (!res.ok) {
    throw new Error('Failed to fetch all products');
  }
  const data = await res.json();
  return data.products ?? [];
};

export const searchProducts = async (search: string): Promise<Product[]> => {
  const params = new URLSearchParams({ search });

  const response = await fetch(
    `${process.env.API_EXTERNAL}/products/search?${params.toString()}`,
    {
      method: 'GET',
      cache: 'no-store',
    },
  );

  if (!response.ok) {
    throw new Error('Failed to search products');
  }
  const data = await response.json();
  return data.products ?? [];
};

export const fetchAdminProductDetails = async (productId: string) => {
  const headerList = await headers();
  const res = await fetch(
    `${process.env.API_EXTERNAL}/product/fetchAdminProductDetails/${productId}`,
    {
      method: 'GET',
      headers: headerList,
      cache: 'no-store',
    },
  );
  if (!res.ok) {
    throw Error(await res.text());
  }
  const data = await res.json();
  return data;
};

export const updateProductImageAction = async (
  prevState: any,
  formData: FormData,
) => {
  const headerList = await headers();
  try {
    const image = formData.get('image') as File;
    const productId = formData.get('id') as string;
    const oldImageUrl = formData.get('url') as string;

    const validatedFile = validateWithZodSchema(imageSchema, { image });
    const fullPath = await uploadImage(validatedFile.image);
    await deleteImage(oldImageUrl);

    const res = await fetch(
      `${process.env.API_EXTERNAL}/product/updateProductImage/${productId}`,
      {
        method: 'GET',
        headers: headerList,
        cache: 'no-store',
        body: JSON.stringify(fullPath),
      },
    );
    if (!res.ok) {
      renderError(res.json);
    }
    // The edit page is server-rendered, so invalidate its RSC cache after the
    // backend accepts the new image URL.
    revalidatePath(`/admin/products/${productId}/edit`);
    return { message: 'Product Image updated successfully' };
  } catch (error) {
    return renderError(error);
  }
};

export const updateProductAction = async (
  prevState: any,
  formData: FormData,
) => {
  const headerList = await headers();
  try {
    const productId = formData.get('id') as string;
    const rawData = Object.fromEntries(formData);

    const validatedFields = validateWithZodSchema(productSchemaBase, rawData);

    const res = await fetch(
      `${process.env.API_EXTERNAL}/product/updateProductImage/${productId}`,
      {
        method: 'GET',
        headers: headerList,
        cache: 'no-store',
        body: JSON.stringify(validatedFields),
      },
    );
    if (!res.ok) {
      renderError(await res.json);
    }
    revalidatePath(`/admin/products/${productId}/edit`);
    return { message: 'Product updated successfully' };
  } catch (error) {
    return renderError(error);
  }
};

export const fetchAdminProducts = async () => {
  const headerList = await headers();
  const res = await fetch(
    `${process.env.API_EXTERNAL}/product/fetchAdminProducts`,
    {
      method: 'GET',
      headers: {
        cookie: headerList.get('cookie') ?? '',
        authorization: headerList.get('authorization') ?? '',
        accept: 'application/json',
      },
      cache: 'no-store',
    },
  );
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data?.message || 'Failed to fetch admin products');
  }
  if (!Array.isArray(data)) {
    throw new Error('Invalid products response');
  }
  return data;
};

export type FormResponse = {
  message: string;
  errors?: Record<string, string[] | undefined>;
};
export const deleteProductAction = async (
  prevState: FormResponse | null,
  formData: FormData,
): Promise<FormResponse> => {
  const productId = formData.get('productId') as string;
  const image = formData.get('image') as string;

  try {
    const headerList = await headers();
    const res = await fetch(
      `${process.env.API_EXTERNAL}/product/deleteProduct/${productId}`,
      {
        method: 'DELETE',
        headers: {
          cookie: headerList.get('cookie') ?? '',
          authorization: headerList.get('authorization') ?? '',
          accept: 'application/json',
        },
        cache: 'no-store',
      },
    );
    if (!res.ok) {
      const err = await res.json();
      return renderError(err);
    }
    if (image != '') {
      await deleteImage(image);
    }
    // Keep both admin and storefront product caches in sync after deletion.
    revalidatePath('/admin/products');
    revalidatePath('/products');
    revalidateTag('products', 'max');
    return { message: 'product removed' };
  } catch (error) {
    return renderError(error);
  }
};

export const fetchSingleProduct = async (productId: string) => {
  const product = await fetch(
    `${process.env.API_EXTERNAL}/product/${productId}`,
    {
      cache: 'no-store',
    },
  );

  if (!product.ok) {
    redirect('/products');
  }
  const productData = await product.json();
  return productData;
};
