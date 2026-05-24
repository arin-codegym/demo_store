'use client';
import { useState } from 'react';
import { SubmitButton } from '@/components/form/Buttons';
import FormContainer from '@/components/form/FormContainer';
import { Card } from '@/components/ui/card';
import RatingInput from '@/components/reviews/RatingInput';
import TextAreaInput from '@/components/form/TextAreaInput';
import { Button } from '@/components/ui/button';
import { createReviewAction } from '@/action/review-action';
import { useCurrentUser } from '@/query/auth/useCurrentUser';
function SubmitReview({ productId }: { productId: string }) {
  const [isReviewFormVisible, setIsReviewFormVisible] = useState(false);
  // const { user } = useUser();
  // const { user } = useAuthStore((state) => ({ user: state.user }));
  // const { user } = useAuthStore();
  const { data: user, isLoading, error } = useCurrentUser();
  /* cách dùng ref để can thiệp trục tiếp vào từng element */
  // const formRef = useRef<HTMLFormElement>(null);
  // const handleAction = async (prevState: any, formData: FormData) => {
  //   // Đảm bảo lấy đúng FormData từ ref nếu tham số formData bị lỗi
  //   const currentFormData = formRef.current ? new FormData(formRef.current) : formData;
  //   const allData = {
  //     productId,
  //     authorName: user?.firstName || 'user',
  //     authorImageUrl: user?.imageUrl || '',
  //     rating: Number(currentFormData.get('rating')),
  //     comment: currentFormData.get('comment'),
  //   };

  //   // Gọi Server Action với cấu trúc mà useFormState mong đợi
  //   return createReviewAction(allData);
  // };
  /* Cách dùng bind nếu muốn là RSC nhưng hiện tại file đang là RCC nên kiểu đóng gói Closure*/
  // const dataToBind = {
  //   productId,
  //   authorName: user?.firstName || 'user',
  //   authorImageUrl: user?.imageUrl || '',
  // };
  // const actionBind = createReviewAction.bind(null, dataToBind);

  return (
    <div>
      <Button
        size='lg'
        className='capitalize'
        onClick={() => setIsReviewFormVisible((prev) => !prev)}
      >
        leave review
      </Button>
      {isReviewFormVisible && (
        <Card className='p-8 mt-8'>
          {/* <FormContainer action={actionBind}> // dùng bind */}
          {/* <input type='hidden' name='productId' value={productId} /> // cách dùng củ chuối của tutorial
            <input
              type='hidden'
              name='authorName'
              value={user?.firstName || 'user'}
            />
            <input
              type='hidden'
              name='authorImageUrl'
              value={user?.imageUrl || ''}
            />  */}

          <FormContainer
            action={async (prevState: any, formData: FormData) => {
              // cách Closure lưu ý bắt buộc component phải là client còn nếu muốn server phải dùng bind()
              // <--- Thêm prevState vào đây
              // Lúc này, tham số thứ 2 (formData) mới đúng là FormData thật của trình duyệt
              // const rating = formData.get('rating');
              // const comment = formData.get('comment');

              const allData = {
                productId,
                authorName: user?.fullName || 'user',
                authorImageUrl: user?.avatarUrl || '',
                // rating: Number(rating),
                // comment: comment as string,
              };

              // Gọi Action của bạn
              return createReviewAction(allData, formData);
            }}
          >
            <RatingInput name='rating' />
            <TextAreaInput
              name='comment'
              labelText='feedback'
              defaultValue='Outstanding product!!!'
            />
            <SubmitButton className='mt-4' />
          </FormContainer>
        </Card>
      )}
    </div>
  );
}

export default SubmitReview;
