import { fetchAllProducts } from '@/action/product-action';
import ProductsShell from './ProductsShell';

// async function ProductsPage({
//   searchParams,
// }: {
//   searchParams: { layout?: string; search?: string };
// }) {
//   // 1. CHỈ CẦN AWAIT MỘT LẦN: searchParams là một Promise
//   const params = await searchParams;
//   const layout = params.layout || 'grid';
//   const search = params.search || '';
//   return (
//     <>
//       <ProductsContainer layout={layout} search={search} />
//     </>
//   );
// }

export default async function ProductsPage() {
  const initialProducts = await fetchAllProducts();

  return <ProductsShell initialProducts={initialProducts} />;
}
