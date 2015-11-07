package adv.android_11.solleks.homework2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;

/**
 * Created by Константин on 07.11.2015.
 */
class Image extends Thread {
    private Bitmap mBitmap;
    private String mImageFile;
    private int mImageID;
    private ImageLoadingTask mLoadingTask;
    private MainActivityFragment.GalleryAdapter mGalleryAdapter;
    private MemoryCache mMemoryCache;

    Image() {
        mImageID = 0;
        mImageFile = null;
        mBitmap = null;
        mLoadingTask = null;
    }

    Image(String imageFile) {
        this.mImageID = 0;
        this.mImageFile = imageFile;
        mBitmap = null;
        mLoadingTask = null;
    }

    Image(String imageFile, int image, MainActivityFragment.GalleryAdapter adapter, MemoryCache cache) {
        this.mImageFile = imageFile;
        this.mImageID = image;
        mBitmap = null;
        mLoadingTask = new ImageLoadingTask();
        mGalleryAdapter = adapter;
        mMemoryCache = cache;
    }

    public AsyncTask.Status getLoadingTaskStatus() {
        return mLoadingTask.getStatus();
    }

    public void cancelLoadTask() {
        mLoadingTask.cancel(false);
    }

    public Bitmap getBitmap() { return mBitmap; }

    public int getImageID() {
        return mImageID;
    }

    public String getFile() {
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

    public void upLoad() {
        if (mBitmap == null && (mLoadingTask == null ||  mLoadingTask.getStatus() != AsyncTask.Status.RUNNING)) {
            //mLoadingTask = new ImageLoadingTask();
            try {
                mLoadingTask.execute(mImageFile);
            } catch (Exception e) {}

        }
    }

    @Override
    public void run(){
        // TODO Производить перезагрузку новых битмапов здесь, а не в удалении
        Bitmap bitmap = null;// mMemoryCache.getBitmapFromMemoryCache(mImageFile);

        if (bitmap == null && mBitmap == null) {
            mBitmap = decodeSampledBitmapFromFile(mImageFile, MainActivityFragment.IMAGE_WIDTH, MainActivityFragment.IMAGE_HEIGHT);

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
        }
    }

    // TODO Удалить
    class ImageLoadingTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            String fileName = params[0];

            if (isCancelled()) return null;

            // TODO Производить перезагрузку новых битмапов здесь, а не в удалении
            Bitmap bitmap = mMemoryCache.getBitmapFromMemoryCache(fileName);

            if (bitmap == null && mBitmap == null) {
                mBitmap = decodeSampledBitmapFromFile(fileName, MainActivityFragment.IMAGE_WIDTH, MainActivityFragment.IMAGE_HEIGHT);

                int orientation = 0;
                try {
                    ExifInterface exif = new ExifInterface(fileName);
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
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mGalleryAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
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