'use client';
import { useState, useEffect, useTransition } from 'react';
import { FavoriteIconButton } from '../form/Buttons';
import { Button } from '../ui/button';
import { fetchWithAuth } from '@/lib/fetchWithAuth.client';
import { useCurrentUser } from '@/query/auth/useCurrentUser';
import { toast } from '../ui/use-toast';

type Props = {
  productId: string;
  favoriteId: string | null;
  onChanged?: (nextFavoriteId: string | null) => void;
};
function FavoriteToggleButton({ productId, favoriteId, onChanged }: Props) {
  const [currentFavoriteId, setCurrentFavoriteId] = useState<string | null>(
    favoriteId,
  );
  const [isPending, startTransition] = useTransition();
  const { data: user } = useCurrentUser();

  useEffect(() => {
    setCurrentFavoriteId(favoriteId);
  }, [favoriteId]);

  async function handleToggle(e: React.MouseEvent) {
    e.preventDefault();
    e.stopPropagation();

    if (!user) {
      toast({
        variant: 'destructive',
        description: 'Vui lòng đăng nhập để thêm vào yêu thích',
      });
      return;
    }
    startTransition(async () => {
      try {
        if (currentFavoriteId) {
          const res = await fetchWithAuth(
            `/api/favorites/delete/${currentFavoriteId}`,
            {
              method: 'DELETE',
              cache: 'no-store',
            },
          );

          if (!res.ok) throw new Error('Failed to remove favorite');

          setCurrentFavoriteId(null);
          onChanged?.(null);
        } else {
          const res = await fetchWithAuth(`/api/favorites/add/${productId}`, {
            method: 'POST',
            cache: 'no-store',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify(productId),
          });

          // if (!res.ok) throw new Error('Failed to add favorite');

          const data: { favoriteId: string } = await res.json();

          setCurrentFavoriteId(data.favoriteId);
          onChanged?.(data.favoriteId);
        }
      } catch (error) {
        console.log(error);
        throw error;
      }
    });
  }

  return (
    <Button
      type='button'
      onClick={handleToggle}
      disabled={isPending}
      className='rounded-full bg-white/80 p-2'
    >
      <FavoriteIconButton
        isFavorite={!!currentFavoriteId}
        isPending={isPending}
      />
    </Button>
  );
}
export default FavoriteToggleButton;
