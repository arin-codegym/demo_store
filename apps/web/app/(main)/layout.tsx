// app/(main)/layout.tsx
import Navbar from '@/components/navbar/Navbar';
import Container from '@/components/global/Container';
import Footer from '@/components/global/Footer';

export default function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className='flex min-h-screen flex-col'>
      <Navbar showSearch />
      <Container className='flex-1 py-20'>{children}</Container>
      <Footer />
    </div>
  );
}
