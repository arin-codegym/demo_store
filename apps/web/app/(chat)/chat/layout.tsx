// app/(dashboard)/layout.tsx
import Navbar from '@/components/navbar/Navbar';
import Container from '@/components/global/Container';

export default function ChatLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <>
      <Navbar showSearch={false} />
      <Container>{children}</Container>
    </>
  );
}
