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
  const { data: user } = useCurrentUser();

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
          <FormContainer
            action={async (prevState: any, formData: FormData) => {
              // Keep user metadata in the client closure; the server action
              // still receives the browser FormData as its second argument.
              const allData = {
                productId,
                authorName: user?.fullName || 'user',
                authorImageUrl: user?.avatarUrl || '',
              };

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
