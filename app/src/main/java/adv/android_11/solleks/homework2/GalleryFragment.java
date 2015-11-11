package adv.android_11.solleks.homework2;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A placeholder fragment containing a simple view.
 */
public class GalleryFragment extends Fragment {

    public static int IMAGE_WIDTH;
    public static int IMAGE_HEIGHT;
    public static final String TAG = "adv.android_11.solleks.homework2.GalleryFragment";

    private GalleryAdapter galleryAdapter;
    private GridView gridView;

    // LruCache<String, Bitmap> для изображений
    private MemoryCache mMemoryCache;
    private boolean mSliding;

    private Image[] data = new Image[0];
    //private Stack<Integer> freeBitmapsID;

    private String path;

    private OnDetailFragmentListener onDetailFragmentListener;

    public void changePath(String path) {
        this.path = path;
        if (gridView != null)
            gridView.setSelection(0);
        reLoadData(new File(path));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onDetailFragmentListener = (OnDetailFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " должен реализовывать интерфейс OnDetailFragmentListener");
        }
    }

    public void openGalleryFragment(String fileName) {
        onDetailFragmentListener.onDetailFragmentOpener(fileName);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //freeBitmapsID = new Stack<>();

        // Создание кеша
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new MemoryCache(cacheSize);
    }

    public void reLoadData(File dir) {
        File[] imageList = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return ((name.endsWith(".jpg"))||(name.endsWith(".png")));
            }
        });

        data = new Image[imageList.length];
        for (int i = 0; i < imageList.length; i++) {
            data[i] = new Image(imageList[i].getPath(), i, mMemoryCache);
            data[i].setDimens(IMAGE_WIDTH, IMAGE_HEIGHT);
        }
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
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openGalleryFragment(((Image)galleryAdapter.getItem(position)).getFile());
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
        }

        public void upDate(int firstVisibleItem, int visibleItemCount) {
            try {
                if (imageLoader != null && imageLoader.getStatus() == AsyncTask.Status.RUNNING) {
                    imageLoader.cancel(false);
                }
                imageLoader = new ImageLoader();
                imageLoader.execute(firstVisibleItem, visibleItemCount);
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

    public String getOpenedDir() {
        return path;
    }

    public interface OnDetailFragmentListener {
        void onDetailFragmentOpener(String fileName);
    }

}
