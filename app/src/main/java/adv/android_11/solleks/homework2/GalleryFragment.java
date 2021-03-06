package adv.android_11.solleks.homework2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;


public class GalleryFragment extends Fragment {

    public static int IMAGE_WIDTH;
    public static int IMAGE_HEIGHT;
    public static final String TAG = "adv.android_11.solleks.homework2.GalleryFragment";

    private GalleryAdapter galleryAdapter;
    private GridView gridView;

    // LruCache<String, Bitmap> для изображений
    private MemoryCache mMemoryCache;
    private boolean mSliding;

    private boolean isLoading;
    private Image[] mData = new Image[0];
    private Stack<Bitmap> mFreeBitmaps;

    private String mPath;

    // Переменные для мультивыбора
    private HashSet<Integer> mSelectedItems;
    private boolean mIsNowSelecting;

    private OnGalleryFragmentListener onGalleryFragmentListener;

    public void changePath(String path) {
        isLoading = true;
        galleryAdapter.notifyDataSetChanged();
        gridView.post(new Runnable() {
            @Override
            public void run() {
                // TODO !!!!!!! Заменить на что то более пригодное T_T !!!!!!!!!
                //gridView.smoothScrollToPosition(0);

            }
        });
        this.mPath = path;
        // Удаление предыдущих данных

        for (Image image : mData) {
            if (image.getBitmap() != null)
                mFreeBitmaps.push(image.getBitmap());
        }

        if (gridView != null)
            gridView.setSelection(0);
        reLoadData(new File(path));
        // Запуск обновления нового контента
        mSliding = true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onGalleryFragmentListener = (OnGalleryFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " должен реализовывать интерфейс OnDetailFragmentListener");
        }
    }

    public void openGalleryFragment(String fileName) {
        onGalleryFragmentListener.onDetailFragmentOpener(fileName);
    }

    public void showMoveButton(boolean show) {
        onGalleryFragmentListener.showMoveButton(show);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFreeBitmaps = new Stack<>();
        mSelectedItems = new HashSet<>();
        isLoading = false;

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

        mData = new Image[imageList.length];
        isLoading = false;
        for (int i = 0; i < imageList.length; i++) {
            mData[i] = new Image(imageList[i].getPath(), i, mMemoryCache);
            if (!mFreeBitmaps.empty()) {
                mData[i].setBitmap(mFreeBitmaps.pop());
                mData[i].setReUseBitmap(true);
            }
            mData[i].setDimens(IMAGE_WIDTH, IMAGE_HEIGHT);
        }
        /* else {
            // Если уже существует массив с битмапами, используем его еще раз
            int length = imageList.length > mData.length ? mData.length : imageList.length;
            for (int i = 0; i < length; i++) {
                mData[i].setImageFile(imageList[i].getPath());
                mData[i].setReUseBitmap(true);
            }
            for (int i = length; i < mData.length; i++) {
                mData[i].recycleBitmap();
            }
            Image[] tempData = new Image[imageList.length];
            System.arraycopy(mData, 0, tempData, 0, length);
            for (int i = length; i < imageList.length; i++) {
                tempData[i] = new Image(imageList[i].getPath(), i, mMemoryCache);
                tempData[i].setDimens(IMAGE_WIDTH, IMAGE_HEIGHT);
            }
            mData = tempData;
        }*/
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
                if (mIsNowSelecting) {
                    if (mSelectedItems.contains(position)) {
                        mSelectedItems.remove(position);
                        if (mSelectedItems.size() == 0) {
                            mIsNowSelecting = false;
                            showMoveButton(false);
                        }
                    }
                    else
                        mSelectedItems.add(position);
                    galleryAdapter.notifyDataSetChanged();
                } else
                    openGalleryFragment(((Image) galleryAdapter.getItem(position)).getFileName());
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mIsNowSelecting) {
                    if (mSelectedItems.contains(position)) {
                        mSelectedItems.remove(position);
                        if (mSelectedItems.size() == 0) {
                            mIsNowSelecting = false;
                            showMoveButton(false);
                        }
                    }
                    else
                        mSelectedItems.add(position);
                    galleryAdapter.notifyDataSetChanged();
                    return false;
                } else {
                    mIsNowSelecting = true;
                    showMoveButton(true);
                    mSelectedItems.add(position);
                    galleryAdapter.notifyDataSetChanged();
                    return true;
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
        }

        public void upDate(int firstVisibleItem, int visibleItemCount) {
            try {
                if (imageLoader != null) {
                    if (imageLoader.getStatus() == AsyncTask.Status.RUNNING) {
                        if (firstVisibleItem == imageLoader.getFirstFile() &&
                                visibleItemCount == imageLoader.getVisibleFiles()) {
                            return;
                        }
                        imageLoader.cancel(false);
                    }
                }
                imageLoader = new ImageLoader();
                imageLoader.execute(firstVisibleItem, visibleItemCount);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        public void cancelLoading() {
            if (imageLoader != null)
                imageLoader.cancel(true);
        }

        @Override
        public int getCount() {
            return mData.length;
        }

        @Override
        public Object getItem(int position) {
            return mData[position];
        }

        @Override
        public long getItemId(int position) {
            return mData[position].getImageID();
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

            if (!isLoading) {
                if (image.getBitmap() != null && !image.getBitmap().isRecycled())
                    holder.imageView.setImageBitmap(image.getBitmap());
                else {
                    if (!image.isAlive())
                        mSliding = true;
                    holder.imageView.setImageBitmap(null);
                }
            } else holder.imageView.setImageBitmap(null);

            if (mIsNowSelecting && mSelectedItems.contains(position)) {
                holder.imageView.setPadding(5, 5, 5, 5);
                holder.imageView.setBackgroundColor(Color.argb(255, 255, 158, 84));
            } else {
                holder.imageView.setPadding(0,0,0,0);
                holder.imageView.setBackgroundColor(Color.LTGRAY);
            }

            return convertView;
        }


        class ViewHolder {
            public ImageView imageView;
        }
    }

    class ImageLoader extends AsyncTask<Integer, Void, Void> {

        private int firstFile;
        private int visibleFiles;

        public int getFirstFile() {
            return firstFile;
        }

        public int getVisibleFiles() {
            return visibleFiles;
        }

        @Override
        protected Void doInBackground(Integer... params) {

            firstFile = params[0];
            visibleFiles = params[1];

            int loadedFile = firstFile;

            for (int i = firstFile; i < firstFile + visibleFiles; i++) {
                if (isCancelled()) return null;

                if (!mData[i].isAlive())
                    mData[i].run();

                for (int j = loadedFile; j <= i; j++) {
                    if (!mData[j].isAlive() && mData[j].getBitmap() != null) {
                        loadedFile = j;
                        this.publishProgress();
                    }
                }
            }

            while (mData[firstFile + visibleFiles - 1].isAlive() && !isCancelled())
                Thread.yield();
            this.publishProgress();

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            galleryAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (galleryAdapter != null)
                galleryAdapter.notifyDataSetChanged();
        }
    }

    public boolean isIsNowSelecting() {
        return mIsNowSelecting;
    }

    public void cancelSelecting() {
        mIsNowSelecting = false;
        showMoveButton(false);
        mSelectedItems.clear();
        galleryAdapter.notifyDataSetChanged();
    }

    public File[] getSelectedFiles() {
        File[] files = new File[mSelectedItems.size()];

        Iterator<Integer> iterator = mSelectedItems.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            files[i++] = new File(mData[iterator.next()].getFileName());
        }
        return files;
    }

    // Метод, предназванченный для вызова при нажатии кнопки назад
    public void cancelLoading() {
        if (galleryAdapter != null)
            galleryAdapter.cancelLoading();
    }

    public String getOpenedDir() {
        return mPath;
    }

    public interface OnGalleryFragmentListener {
        void onDetailFragmentOpener(String fileName);
        void showMoveButton(boolean show);
    }

}
