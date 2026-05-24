'use server';
import { cookies, headers } from 'next/headers';
import { fetchWithAuthServer } from '@/lib/fetchWithAuth.server';
import { Review } from '@/utils/types';
import { revalidatePath } from 'next/cache';
import { renderError } from '@/lib/error';
import { reviewSchema, validateWithZodSchema } from '@/utils/schemas';

export const findExistingReview = async (productId: string) => {
  const cookie = (await cookies()) || '';
  const accessToken = cookie.get('accessToken')?.value || '';
  try {
    const url = `${process.env.API_EXTERNAL}/review/review-does-not-exist/${productId}`;
    const { response, setCookie } = await fetchWithAuthServer(url, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
      // nếu cần:
      //   cookieHeader: cookies().toString(),
    });
    if (!response.ok) return null;
    return response;
  } catch (err) {
    console.log(err);
  }
};

interface ProductRating {
  rating: number;
  count: number;
}

export const fetchProductRating = async (
  productId: string,
): Promise<ProductRating> => {
  try {
    const res = await fetch(
      `${process.env.API_EXTERNAL}/review/fetchProductRating/${productId}`,
    );
    // Kiểm tra nếu response không ok (ví dụ 404, 500)
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({}));
      console.error('Server Error:', errorData);
      throw new Error(errorData);
      // return { rating: 0, count: 0 };
    }

    const data = await res.json();
    return {
      rating: data.rating,
      count: data.count,
    };
  } catch (err) {
    console.log(err);
    // Trả về object mặc định, đảm bảo hàm LUÔN trả về ProductRating
    return { rating: 0, count: 0 };
  }
};

export const fetchProductReviews = async (
  productId: string,
): Promise<Review[]> => {
  try {
    const res = await fetch(
      `${process.env.API_EXTERNAL}/review/fetchProductReviews/${productId}`,
    );
    // Kiểm tra nếu response không ok (ví dụ 404, 500)
    if (!res.ok) {
      // throw new Error();
      // Thay vì throw Error trống rỗng, hãy thử đọc message từ server nếu có
      const errorData = await res.json().catch(() => ({}));
      console.error('Server Error:', errorData);

      return [];
    }
    const data = await res.json();
    return data;
  } catch (err) {
    console.log(err);
    return [];
  }
};

export const fetchProductReviewsByUser = async () => {
  const cookie = (await cookies()) || '';
  const accessToken = cookie.get('accessToken')?.value || '';
  // const user = await getAuthUser();
  // const reviews = await db.review.findMany({
  //   where: {
  //     clerkId: user.id,
  //   },
  //   select: {
  //     id: true,
  //     rating: true,
  //     comment: true,
  //     product: {
  //       select: {
  //         image: true,
  //         name: true,
  //       },
  //     },
  //   },
  // });
  // return reviews;
  try {
    const { response, setCookie } = await fetchWithAuthServer(
      `${process.env.API_EXTERNAL}/review/fetchProductReviewsByUser`,
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      },
    );
    // Kiểm tra nếu response không ok (ví dụ 404, 500)
    if (!response.ok) throw new Error();
    const data = await response.json();
    return data;
  } catch (err) {
    console.log(err);
    return [];
  }
};

export const deleteReviewAction = async (prevState: { reviewId: string }) => {
  const { reviewId } = prevState;
  // const user = await getAuthUser();
  const cookie = (await cookies()) || '';
  const accessToken = cookie.get('accessToken')?.value || '';

  try {
    await fetchWithAuthServer(
      `${process.env.API_EXTERNAL}/review/deleteReviewByUser/${reviewId}`,
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      },
    );

    revalidatePath('/reviews');
    return { message: 'Review deleted successfully' };
  } catch (error) {
    return renderError(error);
  }
};

export const createReviewAction = async (
  // binData: any, cách dùng bind()
  prevState: any,
  formData: FormData,
) => {
  // const user = await getAuthUser();
  const cookie = (await cookies()) || '';
  const accessToken = cookie.get('accessToken')?.value || '';
  try {
    const rawData = Object.fromEntries(formData);
    // 2. Merge (gộp) bindData và formRawData lại làm một
    const finalData = {
      ...prevState,
      ...rawData,
    };

    const validatedFields = validateWithZodSchema(reviewSchema, finalData);
    const { response } = await fetchWithAuthServer(
      `${process.env.API_EXTERNAL}/review/createReview`,
      {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        body: JSON.stringify(validatedFields),
      },
    );
    if (!response.ok) {
      const message = await response.json();
      return {
        message: message || 'Create review failed',
        errors: {},
      };
    }

    revalidatePath(`/products/${validatedFields.productId}`);
    return { message: 'Review submitted successfully' };
  } catch (error) {
    return renderError(error);
  }
};
