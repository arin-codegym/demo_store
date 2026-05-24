'use client';

import { usePathname } from 'next/navigation';
import FormContainer from '../form/FormContainer';
import { toggleFavoriteAction } from '@/action/favorite-action';
import { CardSubmitButton } from '../form/Buttons';

type FavoriteToggleFormProps = {
  productId: string;
  favoriteId: string | null;
  onChanged?: (nextFavoriteId: string | null) => void;
};

function FavoriteToggleForm({
  productId,
  favoriteId,
}: FavoriteToggleFormProps) {
  const pathname = usePathname();
  const toggleAction = toggleFavoriteAction.bind(null, {
    productId,
    favoriteId,
    pathname,
  });
  return (
    <FormContainer action={toggleAction}>
      <CardSubmitButton isFavorite={favoriteId ? true : false} />
    </FormContainer>
  );
}
export default FavoriteToggleForm;
