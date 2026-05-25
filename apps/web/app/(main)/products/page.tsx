import { fetchAllProducts } from '@/action/product-action';
import ProductsShell from './ProductsShell';

export default async function ProductsPage() {
  // Seed the client shell with server data so the first paint is populated
  // before client-side search/filter interactions run.
  const initialProducts = await fetchAllProducts();

  return <ProductsShell initialProducts={initialProducts} />;
}
