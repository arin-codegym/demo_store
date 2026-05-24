import { fetchProductUserFavorites } from '@/action/favorite-action';
import SectionTitle from '@/components/global/SectionTitle';
import ProductsGrid from '@/components/products/ProductsGrid';

async function FavoritesPage() {
  const { products, favoriteMap } = await fetchProductUserFavorites();
  if (products.length === 0)
    return <SectionTitle text='You have no favorites yet.' />;
  return (
    <div>
      <SectionTitle text='Favorites' />
      <ProductsGrid
        // products={favorites.map((favorite: any) => favorite.product)}
        products={products}
        favoriteMap={favoriteMap}
      />
    </div>
  );
}

export default FavoritesPage;
