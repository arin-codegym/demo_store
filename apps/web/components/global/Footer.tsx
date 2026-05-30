import Link from 'next/link';
import { Mail, Phone } from 'lucide-react';
import Container from './Container';

function Footer() {
  return (
    <footer className='border-t bg-slate-950 text-slate-200'>
      <Container className='grid gap-10 py-10 md:grid-cols-[1.2fr_1fr_1fr]'>
        <div>
          <h2 className='text-base font-semibold uppercase tracking-wide text-white'>
            QuocHuy Developer
          </h2>
          <p className='mt-4 max-w-md text-sm leading-6 text-slate-400'>
            Cửa hàng trực tuyến với các sản phẩm được cập nhật nhanh, giỏ hàng
            tiện lợi và hỗ trợ khách hàng khi cần.
          </p>
        </div>

        <div>
          <h3 className='text-sm font-semibold uppercase tracking-wide text-white'>
            Liên hệ
          </h3>
          <div className='mt-4 space-y-3 text-sm text-slate-400'>
            <a
              href='tel:0963639701'
              className='flex items-center gap-3 transition-colors hover:text-white'
            >
              <Phone className='h-4 w-4 text-primary' />
              <span>0963639701</span>
            </a>
            <a
              href='mailto:caoquochuy21@gmail.com'
              className='flex items-center gap-3 transition-colors hover:text-white'
            >
              <Mail className='h-4 w-4 text-primary' />
              <span>caoquochuy21@gmail.com</span>
            </a>
          </div>
        </div>

        <div>
          <h3 className='text-sm font-semibold uppercase tracking-wide text-white'>
            Điều hướng
          </h3>
          <nav className='mt-4 grid gap-3 text-sm text-slate-400'>
            <Link className='transition-colors hover:text-white' href='/'>
              Trang chủ
            </Link>
            <Link
              className='transition-colors hover:text-white'
              href='/products'
            >
              Sản phẩm
            </Link>
            <Link className='transition-colors hover:text-white' href='/about'>
              Giới thiệu
            </Link>
          </nav>
        </div>
      </Container>

      <div className='border-t border-white/10'>
        <Container className='py-4 text-center text-xs text-slate-500'>
          © {new Date().getFullYear()} QuocHuy Developer. Bản quyền thuộc về tác
          giả.
        </Container>
      </div>
    </footer>
  );
}

export default Footer;
