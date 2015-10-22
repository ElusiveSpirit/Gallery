package adv.android_11.solleks.homework2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private GalleryAdapter galleryAdapter;
    private GridView gridView;

    private LruCache<String, Bitmap> mMemoryCache;
    private boolean mSliding;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
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
                    Toast.makeText(inflater.getContext(), "Scrolling", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Создание кеша
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        return view;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap){
        if (getBitmapFromMemoryCache(key) == null)
            mMemoryCache.put(key, bitmap);
    }

    public Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    class GalleryAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        private Image[] data;
        private Bitmap[] visibleData;

        private ImageLoadingTask imageLoadingTask;

        public GalleryAdapter(LayoutInflater inflater) {
            super();
            mInflater = inflater;

            visibleData = new Bitmap[20];

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
                data[i] = new Image(imageList[i].getPath());
            }

        }

        public void upDate(int firstVisibleItem, int visibleItemCount) {
            if (imageLoadingTask != null && imageLoadingTask.getStatus() == AsyncTask.Status.RUNNING)
                imageLoadingTask.cancel(false);

            imageLoadingTask = new ImageLoadingTask();
            imageLoadingTask.execute(firstVisibleItem, visibleItemCount);
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
            return position;
        }

       /* @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            Image item = (Image) getItem(position);

            addBitmapToMemoryCache(item.getFile(), visibleData[item.getImageID()]);

            visibleData[item.getImageID()] = null;

            return convertView;
        }*/

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

            if (getBitmapFromMemoryCache(image.getFile()) != null)
                holder.imageView.setImageBitmap(getBitmapFromMemoryCache(image.getFile()));
            else
                holder.imageView.setImageBitmap(null);

            return convertView;
        }


        class ViewHolder {
            public ImageView imageView;
        }

        class ImageLoadingTask extends AsyncTask<Integer, Void, Void> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Integer... params) {
                final int firstPosition = params[0];
                final int viewCount = params[1];

                for (int i = firstPosition; i < (firstPosition + viewCount); i++) {
                    if (isCancelled()) return null;
                    if (getBitmapFromMemoryCache(data[i].getFile()) == null)
                        addBitmapToMemoryCache(data[i].getFile(), decodeSampledBitmapFromFile(data[i].getFile(),
                              mInflater.getContext().getResources().getDimensionPixelSize(R.dimen.image_width),
                              mInflater.getContext().getResources().getDimensionPixelSize(R.dimen.image_height)));
                   /* data[i].setImageID(decodeSampledBitmapFromFile(params[i].getPath(),
                            mInflater.getContext().getResources().getDimensionPixelSize(R.dimen.image_width),
                            mInflater.getContext().getResources().getDimensionPixelSize(R.dimen.image_height)));*/
                    publishProgress();
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate();
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

        class Image {

            private String mImageFile;
            private int mImageID;

            Image() {
                mImageID = 0;
                mImageFile = null;
            }

            Image(String imageFile) {
                this.mImageID = 0;
                this.mImageFile = imageFile;
            }

            Image(String imageFile, int image) {
                this.mImageFile = imageFile;
                this.mImageID = image;
            }

            public int getImageID() {
                return mImageID;
            }

            public String getFile() {
                return mImageFile;
            }

            public void setImageID(int Image) {
                this.mImageID = Image;
            }

            public void setImageFile(String ImageFile) {
                this.mImageFile = ImageFile;
            }
        }
    }


}
