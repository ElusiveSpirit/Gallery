package adv.android_11.solleks.homework2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Stack;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private int IMAGE_WIDTH;
    private int IMAGE_HEIGHT;

    private GalleryAdapter galleryAdapter;
    private GridView gridView;

    private LruCache<String, Bitmap> mMemoryCache;
    private boolean mSliding;

    private Image[] data;
    private Stack<Integer> freeBitmapsID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        freeBitmapsID = new Stack<>();

        // Загрузка файлов
        File rootSD = Environment.getExternalStorageDirectory();
        File DCIM = new File(rootSD.getAbsolutePath() + "/DCIM/Camera");

        File[] imageList = DCIM.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return ((name.endsWith(".jpg"))||(name.endsWith(".png")));
            }
        });


        data = new Image[imageList.length];
        for (int i = 0; i < imageList.length; i++) {
            data[i] = new Image(imageList[i].getPath(), i);
        }

        // Создание кеша
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        setRetainInstance(true);
      /*  IMAGE_WIDTH = inflater.getContext().getResources().getDimensionPixelSize(R.dimen.image_width);
        IMAGE_HEIGHT = inflater.getContext().getResources().getDimensionPixelSize(R.dimen.image_height);*/
        IMAGE_WIDTH = IMAGE_HEIGHT = 100;

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        gridView = (GridView)view.findViewById(R.id.gridView);

        galleryAdapter = new GalleryAdapter(inflater);
        gridView.setAdapter(galleryAdapter);

        mSliding = true;
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mSliding = true;

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mSliding && visibleItemCount != 0) {
                    mSliding = false;
                    galleryAdapter.upDate(firstVisibleItem, visibleItemCount);
                }
            }
        });

        return view;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap){
        if (getBitmapFromMemoryCache(key) == null)
            mMemoryCache.put(key, bitmap);
    }

    public Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    class GalleryAdapter extends BaseAdapter{

        private LayoutInflater mInflater;

        public GalleryAdapter(LayoutInflater inflater) {
            super();
            mInflater = inflater;

        }

        public void upDate(int firstVisibleItem, int visibleItemCount) {

            for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
                data[i].upLoad();
            }
        }

        @Override
        public int getCount() {
            return data.length;
        }

        @Override
        public Object getItem(int position) {
            return data[position];
        }

        @Override
        public long getItemId(int position) {
            return data[position].getImageID();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_main, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Image image = (Image)getItem(position);

            holder.imageView.setMaxHeight(holder.imageView.getWidth());
            holder.imageView.setImageBitmap(image.getBitmap());


            return convertView;
        }


        class ViewHolder {
            public ImageView imageView;
        }
    }

    class Image {
        private Bitmap mBitmap;
        private String mImageFile;
        private int mImageID;
        private ImageLoadingTask mLoadingTask;

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

        Image(String imageFile, int image) {
            this.mImageFile = imageFile;
            this.mImageID = image;
            mBitmap = null;
            mLoadingTask = new ImageLoadingTask();;
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
                mLoadingTask.execute(mImageFile);
            }
        }

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
                Bitmap bitmap = getBitmapFromMemoryCache(fileName);

                if (bitmap == null && mBitmap == null) {
                    mBitmap = decodeSampledBitmapFromFile(fileName, IMAGE_WIDTH, IMAGE_HEIGHT);

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

                galleryAdapter.notifyDataSetChanged();
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

    }

}
