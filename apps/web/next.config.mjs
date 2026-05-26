/** @type {import('next').NextConfig} */
const staticImageHosts = [
  'images.pexels.com',
  '*.imgix.net',
  'obtcbjxdxicipamogdre.supabase.co',
  'd3vzq0a0zf11gd.cloudfront.net',
  'demo-store-product-images-prod-11111989.s3.ap-southeast-2.amazonaws.com',
  'img.clerk.com',
  '*.googleusercontent.com',
];

const imageBaseUrls = [
  process.env.S3_PRODUCT_IMAGES_PUBLIC_BASE_URL,
  process.env.S3_PRODUCT_IMAGES_BUCKET && process.env.AWS_REGION
    ? `https://${process.env.S3_PRODUCT_IMAGES_BUCKET}.s3.${process.env.AWS_REGION}.amazonaws.com`
    : undefined,
];

const toRemotePattern = (baseUrl) => {
  if (!baseUrl) return null;

  try {
    const url = new URL(baseUrl);
    if (url.protocol !== 'https:' && url.protocol !== 'http:') return null;

    return {
      protocol: url.protocol.replace(':', ''),
      hostname: url.hostname,
    };
  } catch {
    return null;
  }
};

const remotePatterns = [
  ...staticImageHosts.map((hostname) => ({
    protocol: 'https',
    hostname,
  })),
  ...imageBaseUrls.map(toRemotePattern).filter(Boolean),
].filter(
  (pattern, index, patterns) =>
    patterns.findIndex(
      (item) =>
        item.protocol === pattern.protocol && item.hostname === pattern.hostname,
    ) === index,
);

const nextConfig = {
  logging: {
    fetches: {
      fullUrl: false,
    },
  },
  images: {
    remotePatterns,
  },
  output: 'standalone',
};

export default nextConfig;
