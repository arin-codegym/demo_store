import { imageSchema, validateWithZodSchema } from '@/utils/schemas';
import { deleteImage, uploadImage } from '@/utils/s3';
import { NextRequest, NextResponse } from 'next/server';
import { ValidationError } from '@/utils/schemas';

export const runtime = 'nodejs';

export async function POST(req: NextRequest) {
  const formData = await req.formData();
  const file = formData.get('image') as File;
  try {
    const validatedFile = validateWithZodSchema(imageSchema, { image: file });
    const imageLink = await uploadImage(validatedFile.image);

    return NextResponse.json({ imageLink });
  } catch (err: unknown) {
    if (err instanceof ValidationError) {
      return NextResponse.json(
        {
          message: 'Validation failed',
          errors: err.errors,
        },
        { status: 400 },
      );
    }
    if (err instanceof Error) {
      return NextResponse.json(
        {
          message: err.message,
        },
        { status: 500 },
      );
    }

    return NextResponse.json(
      {
        message: 'Unknown error',
      },
      { status: 500 },
    );
  }
}

export async function DELETE(req: NextRequest) {
  const { imageUrl } = await req.json();
  try {
    const result = await deleteImage(imageUrl);
    // console.log('DELETE RESULT:', result);
    return NextResponse.json({ ok: true, result });
  } catch (error) {
    // console.error('DELETE ERROR:', error);
    return NextResponse.json(
      { ok: false, error: String(error) },
      { status: 500 },
    );
  }
}
