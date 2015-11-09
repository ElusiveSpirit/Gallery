package adv.android_11.solleks.homework2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
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

    public static int IMAGE_WIDTH;
    public static int IMAGE_HEIGHT;

    private GalleryAdapter galleryAdapter;
    private GridView gridView;

    // LruCache<String, Bitmap> для изображений
    private MemoryCache mMemoryCache;
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
            data[i] = new Image(imageList[i].getPath(), i, mMemoryCache);
        }

        // Создание кеша
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new MemoryCache(cacheSize);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        setRetainInstance(true);

        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
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

    public class GalleryAdapter extends BaseAdapter{

        private LayoutInflater mInflater;
        private ImageLoader imageLoader;


        public GalleryAdapter(LayoutInflater inflater) {
            super();
            mInflater = inflater;
            imageLoader = new ImageLoader();
        }

        public void upDate(int firstVisibleItem, int visibleItemCount) {
            try {
                if (imageLoader != null) {
                    if (imageLoader.getStatus() == AsyncTask.Status.RUNNING) {
                        imageLoader.cancel(false);
                        imageLoader = new ImageLoader();
                }
                imageLoader.execute(firstVisibleItem, visibleItemCount);
            }
            } catch (NullPointerException e) {
                e.printStackTrace();
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

    class ImageLoader extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            int
                firstFile = params[0],
                visibleFiles = params[1],
                loadedFile = 0;

            for (int i = firstFile; i < firstFile + visibleFiles; i++) {
                if (isCancelled()) return null;

                data[i].run();

                for (int j = loadedFile; j <= i; j++) {
                    if (!data[j].isAlive() && data[j].getBitmap() != null) {
                        loadedFile = j;
                        this.publishProgress();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            galleryAdapter.notifyDataSetChanged();
        }
    }

}
