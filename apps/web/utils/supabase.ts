import { createClient } from '@supabase/supabase-js';

const bucket = 'main-bucket';

// Create a single supabase client for interacting with your database
export const supabase = createClient(
  process.env.SUPABASE_URL as string,
  process.env.SUPABASE_KEY as string,
  //   process.env.NEXT_PUBLIC_SUPABASE_URL as string, nếu muốn import ra client
  // process.env.NEXT_PUBLIC_SUPABASE_KEY as string,
);

export const uploadImage = async (image: File) => {
  const timestamp = Date.now();
  // const newName = `/users/${timestamp}-${image.name}`;
  const newName = `${timestamp}-${image.name}`;

  const { data, error } = await supabase.storage
    .from(bucket)
    .upload(newName, image, {
      cacheControl: '3600',
    });
  if (!data) throw new Error('Image upload failed');
  return supabase.storage.from(bucket).getPublicUrl(newName).data.publicUrl;
};

export const deleteImage = async (url: string) => {
  const imageName = decodeURIComponent(url.split('/').pop() || '');
  if (!imageName) throw new Error('Invalid URL');
  // return supabase.storage.from(bucket).remove([imageName]);
  const p = supabase.storage.from(bucket).remove([imageName]);
  const result = await p;

  return result;
};
