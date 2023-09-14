package top.zibin.luban;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Responsible for starting compress and managing active and cached resources.
 */
class Engine {
    private InputStreamProvider srcImg;
    private File tagImg;
    private int srcWidth;
    private int srcHeight;
    private boolean focusAlpha;

    Engine(InputStreamProvider srcImg, File tagImg, boolean focusAlpha) throws IOException {
        this.tagImg = tagImg;
        this.srcImg = srcImg;
        this.focusAlpha = focusAlpha;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;

        BitmapFactory.decodeStream(srcImg.open(), null, options);
        this.srcWidth = options.outWidth;
        this.srcHeight = options.outHeight;
    }

    private int computeSize() {
        srcWidth = srcWidth % 2 == 1 ? srcWidth + 1 : srcWidth;
        srcHeight = srcHeight % 2 == 1 ? srcHeight + 1 : srcHeight;

        int longSide = Math.max(srcWidth, srcHeight);
        int shortSide = Math.min(srcWidth, srcHeight);

        float scale = ((float) shortSide / longSide);
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                return 1;
            } else if (longSide < 4990) {
                return 2;
            } else if (longSide > 4990 && longSide < 10240) {
                return 4;
            } else {
                return longSide / 1280;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            return longSide / 1280 == 0 ? 1 : longSide / 1280;
        } else {
            return (int) Math.ceil(longSide / (1280.0 / scale));
        }
    }

    private Bitmap rotatingImage(Bitmap bitmap, int angle) {
        Matrix matrix = new Matrix();

        matrix.postRotate(angle);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * @param maxWidth  maximum width
     * @param maxHeight maximum height
     * @param maxCompressFileSizeBytes Maximum file size after compression
     * @throws IOException
     */
    File compress(int maxWidth, int maxHeight, long maxCompressFileSizeBytes) throws IOException {
        if (maxWidth != 0 && maxHeight != 0) {
            return compressWithMaxDimensions(maxWidth, maxHeight);
        }
        if (maxCompressFileSizeBytes != 0) {
            return compressWithMaxFileSize(maxCompressFileSizeBytes);
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = computeSize();

        Bitmap tagBitmap = BitmapFactory.decodeStream(srcImg.open(), null, options);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        if (Checker.SINGLE.isJPG(srcImg.open())) {
            tagBitmap = rotatingImage(tagBitmap, Checker.SINGLE.getOrientation(srcImg.open()));
        }
        tagBitmap.compress(focusAlpha || tagBitmap.hasAlpha() ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 60, stream);
        tagBitmap.recycle();

        FileOutputStream fos = new FileOutputStream(tagImg);
        fos.write(stream.toByteArray());
        fos.flush();
        fos.close();
        stream.close();

        return tagImg;
    }


    /**
     * Compress according to maximum width and height restrictions
     */
    File compressWithMaxDimensions(int maxWidth, int maxHeight) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = computeSize();

        Bitmap tagBitmap = BitmapFactory.decodeStream(srcImg.open(), null, options);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        if (Checker.SINGLE.isJPG(srcImg.open())) {
            tagBitmap = rotatingImage(tagBitmap, Checker.SINGLE.getOrientation(srcImg.open()));
        }

        // Check if the image dimensions exceed the maximum limits
        int currentWidth = tagBitmap.getWidth();
        int currentHeight = tagBitmap.getHeight();
        while (currentWidth > maxWidth || currentHeight > maxHeight) {
            // Reduce the quality and compress the image again
            int quality = 60;  // You can adjust the quality as needed
            stream.reset();
            tagBitmap.compress(focusAlpha || tagBitmap.hasAlpha() ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, quality, stream);

            // Calculate the new sample size to further reduce dimensions
            options.inSampleSize *= 2;
            tagBitmap = BitmapFactory.decodeStream(srcImg.open(), null, options);
            currentWidth = tagBitmap.getWidth();
            currentHeight = tagBitmap.getHeight();
        }

        tagBitmap.compress(focusAlpha || tagBitmap.hasAlpha() ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 60, stream);
        tagBitmap.recycle();

        FileOutputStream fos = new FileOutputStream(tagImg);
        fos.write(stream.toByteArray());
        fos.flush();
        fos.close();
        stream.close();

        return tagImg;
    }

    /**
     * Compress based on maximum file size
     */
    File compressWithMaxFileSize(long maxFileSizeBytes) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = computeSize();

        Bitmap tagBitmap = BitmapFactory.decodeStream(srcImg.open(), null, options);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        if (Checker.SINGLE.isJPG(srcImg.open())) {
            tagBitmap = rotatingImage(tagBitmap, Checker.SINGLE.getOrientation(srcImg.open()));
        }

        int quality = 60; // 初始质量
        boolean compressing = true;
        while (compressing) {
            stream.reset();
            tagBitmap.compress(focusAlpha || tagBitmap.hasAlpha() ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, quality, stream);
            if ( stream.size() > maxFileSizeBytes) {
                // If file size exceeds limit, reduce quality and continue compression
                quality -= 10; // Reduce mass, can be adjusted as needed
                if (quality < 0) {
                    tagBitmap.recycle();
//                    tagBitmap.recycle();
//                    FileOutputStream fos = new FileOutputStream(tagImg);
//                    fos.write(stream.toByteArray());
//                    fos.flush();
//                    fos.close();
//                    stream.close();
//                    return tagImg;
                    throw new IOException("Unable to compress image within the desired file size limit.");
                }
            } else {
                compressing = false; // File size is within the limit, stop compressing
            }
        }
        tagBitmap.recycle();
        FileOutputStream fos = new FileOutputStream(tagImg);
        fos.write(stream.toByteArray());
        fos.flush();
        fos.close();
        stream.close();

        return tagImg;
    }

}