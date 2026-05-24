import { Separator } from '@/components/ui/separator';
import Sidebar from './Sidebar';
import { AdminGuard } from '@/components/auth/AdminGuard';
import Navbar from '@/components/navbar/Navbar';
import Container from '@/components/global/Container';

function DashboardLayout({ children }: { children: React.ReactNode }) {
  return (
    <AdminGuard>
      <Navbar showSearch={false} />
      <Container className='py-3'>
        {/* <Separator className='mt-2' /> */}
        <h2 className='text-2xl pl-4'>Dashboard</h2>
        <section className='grid lg:grid-cols-12 gap-12 mt-12'>
          <div className='lg:col-span-2'>
            <Sidebar />
          </div>
          <div className='lg:col-span-10 px-4'>{children}</div>
        </section>
      </Container>
    </AdminGuard>
  );
}
export default DashboardLayout;
