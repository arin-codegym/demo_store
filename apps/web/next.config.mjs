/** @type {import('next').NextConfig} */
const nextConfig = {
  logging: {
    fetches: {
      fullUrl: false, // Để true sẽ log đầy đủ URL, false sẽ gọn hơn
    },
  },
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'images.pexels.com',
      },
      {
        protocol: 'https',
        hostname: '*.imgix.net',
      },
      {
        protocol: 'https',
        hostname: 'obtcbjxdxicipamogdre.supabase.co',
      },
      {
        protocol: 'https',
        hostname:
          'demo-store-product-images-prod-11111989.s3.ap-southeast-2.amazonaws.com',
      },
      {
        protocol: 'https',
        hostname: 'img.clerk.com',
      },
      {
        protocol: 'https',
        hostname: '*.googleusercontent.com',
      },
    ],
  },
  output: 'standalone',
};

export default nextConfig;
