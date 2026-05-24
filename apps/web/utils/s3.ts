import {
  DeleteObjectCommand,
  PutObjectCommand,
  S3Client,
} from '@aws-sdk/client-s3';

let s3Client: S3Client | null = null;

const getS3Config = () => {
  const region = process.env.AWS_REGION;
  const bucket = process.env.S3_PRODUCT_IMAGES_BUCKET;
  const publicBaseUrl = process.env.S3_PRODUCT_IMAGES_PUBLIC_BASE_URL;

  if (!region) {
    throw new Error('AWS_REGION is required');
  }

  if (!bucket) {
    throw new Error('S3_PRODUCT_IMAGES_BUCKET is required');
  }

  return { region, bucket, publicBaseUrl };
};

const getS3Client = (region: string) => {
  if (!s3Client) {
    s3Client = new S3Client({ region });
  }

  return s3Client;
};

export const uploadImage = async (image: File) => {
  const { region, bucket, publicBaseUrl } = getS3Config();
  const key = `products/${Date.now()}-${sanitizeFileName(image.name)}`;
  const body = Buffer.from(await image.arrayBuffer());

  await getS3Client(region).send(
    new PutObjectCommand({
      Bucket: bucket,
      Key: key,
      Body: body,
      ContentType: image.type || 'application/octet-stream',
      CacheControl: 'public, max-age=31536000, immutable',
    }),
  );

  return buildPublicUrl(key, bucket, region, publicBaseUrl);
};

export const deleteImage = async (url: string) => {
  const { region, bucket } = getS3Config();
  const key = getObjectKeyFromUrl(url);

  await getS3Client(region).send(
    new DeleteObjectCommand({
      Bucket: bucket,
      Key: key,
    }),
  );

  return { key };
};

const buildPublicUrl = (
  key: string,
  bucket: string,
  region: string,
  publicBaseUrl?: string,
) => {
  const baseUrl =
    publicBaseUrl ?? `https://${bucket}.s3.${region}.amazonaws.com`;

  return `${baseUrl.replace(/\/$/, '')}/${encodeObjectKey(key)}`;
};

const getObjectKeyFromUrl = (url: string) => {
  if (!url) {
    throw new Error('Image URL is required');
  }

  const parsedUrl = new URL(url);
  const key = decodeURIComponent(parsedUrl.pathname.replace(/^\/+/, ''));

  if (!key) {
    throw new Error('Invalid image URL');
  }

  return key;
};

const encodeObjectKey = (key: string) => {
  return key.split('/').map(encodeURIComponent).join('/');
};

const sanitizeFileName = (fileName: string) => {
  const sanitized = fileName
    .toLowerCase()
    .replace(/[^a-z0-9._-]+/g, '-')
    .replace(/^-+|-+$/g, '');

  return sanitized || 'upload.bin';
};
