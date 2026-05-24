import { fetchFeaturedProducts } from '@/action/product-action';
import EmptyList from '../global/EmptyList';
import SectionTitle from '../global/SectionTitle';
import { FeaturedProductChild } from './FeaturedProductChild';
async function FeaturedProducts() {
  const products = await fetchFeaturedProducts();
  if (products.length === 0) return <EmptyList />;
  return (
    <section className='pt-24'>
      <SectionTitle text='featured products' />
      <FeaturedProductChild initialProducts={products} />
    </section>
  );
}
export default FeaturedProducts;
