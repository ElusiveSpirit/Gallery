package adv.android_11.solleks.homework2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

/**
 * Created by Константин on 07.11.2015.
 */
class Image extends Thread {
    private Bitmap mBitmap;
    private String mImageFile;
    private int mImageID;
    private MemoryCache mMemoryCache;

    Image() {
        mImageID = 0;
        mImageFile = null;
        mBitmap = null;
    }

    Image(String imageFile) {
        this.mImageID = 0;
        this.mImageFile = imageFile;
        mBitmap = null;
    }

    Image(String imageFile, int image, MemoryCache cache) {
        this.mImageFile = imageFile;
        this.mImageID = image;
        mBitmap = null;
        mMemoryCache = cache;
    }

    public Bitmap getBitmap() { return mBitmap; }

    public int getImageID() {
        return mImageID;
    }

    public String getFile() {
        return mImageFile;
    }

    public String getFileDir() {
        String s[] = mImageFile.split("/");
        if (s.length > 2)
            return s[s.length - 2];
        else
            return mImageFile;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public void setImageID(int Image) {
        this.mImageID = Image;
    }

    public void setImageFile(String ImageFile) {
        this.mImageFile = ImageFile;
    }

    @Override
    public void run(){
        // TODO Производить перезагрузку новых битмапов здесь, а не в удалении
        Bitmap bitmap = null;// mMemoryCache.getBitmapFromMemoryCache(mImageFile);

        // TODO сделать свойство для размера загружаемых картинок
        MainActivityFragment.IMAGE_WIDTH = MainActivityFragment.IMAGE_HEIGHT = 250;
        try {
            if (bitmap == null && mBitmap == null) {
                mBitmap = decodeSampledBitmapFromFile(mImageFile, MainActivityFragment.IMAGE_WIDTH, MainActivityFragment.IMAGE_HEIGHT);

                if (mBitmap != null) {
                    try {
                        int orientation = 0;
                        try {
                            ExifInterface exif = new ExifInterface(mImageFile);
                            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
                            switch (orientation) {
                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    orientation = 270;
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    orientation = 180;
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    orientation = 90;
                                    break;
                                case ExifInterface.ORIENTATION_NORMAL:
                                    orientation = 0;
                                    break;
                                default:
                                    orientation = -1;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Matrix matrix = new Matrix();
                        Bitmap decodedBitmap = mBitmap;
                        if (orientation > 0) {
                            matrix.postRotate(orientation);
                            mBitmap = Bitmap.createBitmap(decodedBitmap, (mBitmap.getWidth() - mBitmap.getHeight()) / 2, 0, mBitmap.getHeight(),
                                    mBitmap.getHeight(), matrix, true);
                        } else {
                            mBitmap = Bitmap.createBitmap(decodedBitmap, (mBitmap.getWidth() - mBitmap.getHeight()) / 2, 0, mBitmap.getHeight(),
                                    mBitmap.getHeight());
                        }

                        if (!decodedBitmap.equals(mBitmap)) {
                            decodedBitmap.recycle();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            mBitmap = null;
        }
    }

    public Bitmap decodeSampledBitmapFromFile(String file, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, options);

        options.inSampleSize = calculateInSampleSize(options, width, height);


        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(file, options);
    }

    public Bitmap decodeSampledBitmapFromFile(String file, int width, int height, Bitmap reUsableBitmap) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, options);

        options.inSampleSize = calculateInSampleSize(options, width, height);

        options.inJustDecodeBounds = false;
        options.inBitmap = reUsableBitmap;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(file, options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight &&
                    (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

        }

        return inSampleSize;
    }
}