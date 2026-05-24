// app/(dashboard)/layout.tsx
import Navbar from '@/components/navbar/Navbar';
import Container from '@/components/global/Container';

export default function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <>
      <Navbar showSearch />
      <Container className='py-20'>{children}</Container>
    </>
  );
}
