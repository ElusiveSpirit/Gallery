package adv.android_11.solleks.homework2;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Константин on 07.11.2015.
 *
 */
public class MainFragment extends Fragment {

    public static int IMAGE_WIDTH;
    public static int IMAGE_HEIGHT;
    public static final String DATA_KEY = "adv.android_11.solleks.homework2.MainFragment.DATA_KEY";
    public static final String TAG = "adv.android_11.solleks.homework2.MainFragment";

    private TempClass temp;

    private Context mAppContext;

    private ArrayList<String> data;
    private ArrayList<Image> dataImage;
    private GalleryAdapter galleryAdapter;

    private boolean isSelectingDir;
    private File[] mFilesToMove;

    private OnFragmentInteractionListener mListener;

    public static MainFragment newInstance(ArrayList<String> data) {
        MainFragment mainFragment = new MainFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(DATA_KEY, data);
        mainFragment.setArguments(args);
        return mainFragment;
    }

    public void setSelectDirToMove(File[] files) {
        isSelectingDir = true;
        mFilesToMove = files;

        Image image = new Image();
        image.setBitmap(BitmapFactory.decodeResource(
                mAppContext.getResources(),
                R.drawable.add_button));
        dataImage.add(image);
        galleryAdapter.notifyDataSetChanged();
    }

    public void setData(ArrayList<String> data) {
        this.data = data;
        File rootSD = Environment.getExternalStorageDirectory();

        if (data != null) {
            dataImage = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                dataImage.add(new Image(data.get(i), i, null));
                dataImage.get(i).setDimens(IMAGE_WIDTH, IMAGE_HEIGHT);
            }
        } else {
            dataImage = new ArrayList<>();
        }
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.execute(rootSD);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " должен реализовывать интерфейс OnFragmentInteractionListener");
        }
    }

    public void openGalleryFragment(String fileName) {
        mListener.onFragmentInteraction(fileName);
    }

    public void upDateData(int originalPath, int newPath) {

        ArrayList<String> originalFile = MainActivity.saveFilesFromDir(new File(dataImage.get(originalPath).getFilePath()));
        ArrayList<String> newFile = MainActivity.saveFilesFromDir(new File(dataImage.get(newPath).getFilePath()));

        AsyncTask<Integer, Void, Void> task = new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                for (Integer i : params) {
                    dataImage.get(i).setReUseBitmap(true);
                    dataImage.get(i).run();
                }
                for (Integer i : params)
                    while (dataImage.get(i).isAlive()) {
                        Thread.yield();
                    }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                galleryAdapter.notifyDataSetChanged();
            }
        };

        if (originalFile != null && newFile != null) {
            if (originalFile.size() == 0 && newFile.size() == 0) {
                if (originalPath > newPath) {
                    dataImage.remove(originalPath);
                    dataImage.remove(newPath);
                } else {
                    dataImage.remove(newPath);
                    dataImage.remove(originalPath);
                }
            } else if ( originalFile.size() == 0) {
                dataImage.remove(originalPath);
                if (originalPath < newPath)
                    newPath--;
                dataImage.get(newPath).setImageFile(newFile.get(0));
                task.execute(newPath);
            } else if (newFile.size() == 0) {
                dataImage.remove(newPath);
                if (newPath < originalPath)
                    originalPath--;
                dataImage.get(originalPath).setImageFile(originalFile.get(0));
                task.execute(originalPath);
            } else {
                dataImage.get(originalPath).setImageFile(originalFile.get(0));
                dataImage.get(newPath).setImageFile(newFile.get(0));
                task.execute(originalPath, newPath);
            }
        }

        galleryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        isSelectingDir = false;

        data = getArguments().getStringArrayList(DATA_KEY);
        setData(data);
    }

    class ImageLoader extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... params) {
            for (Image image : dataImage) {
                image.run();
                publishProgress();
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (galleryAdapter != null)
                galleryAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (galleryAdapter != null)
                galleryAdapter.notifyDataSetChanged();
        }
    }



    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView)view.findViewById(R.id.menuGridView);

        mAppContext = inflater.getContext().getApplicationContext();

        galleryAdapter = new GalleryAdapter(inflater);
        gridView.setAdapter(galleryAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isSelectingDir) {
                    // TODO Добавить "Горение" перемещаемых файлов
                    Image image = ((Image) galleryAdapter.getItem(position));
                    temp = new TempClass(image, position, inflater);
                    if (image.getFileName() == null) {
                        new NewAlbumFragment().show(getFragmentManager(), "tag  ");
                    } else {
                        isSelectingDir = false;
                        temp.saveFiles();
                    }
                } else
                    openGalleryFragment(((Image) galleryAdapter.getItem(position)).getFilePath());
            }
        });
        return view;
    }

    class TempClass {

        private Image image;
        private int position;
        private LayoutInflater inflater;
        private boolean isNewAlbum;

        TempClass(Image image, int position, LayoutInflater inflater) {
            this.image = image;
            this.position = position;
            this.inflater = inflater;
            this.isNewAlbum = false;
        }

        public Image getImage() {
            return image;
        }

        public void setNewAlbum(boolean isNewAlbum) {
            this.isNewAlbum = isNewAlbum;
        }

        public void saveFiles() {
            new File(image.getFilePath() + "/").mkdirs();
            for (File file : mFilesToMove) {
                file.renameTo(new File(image.getFilePath() + "/" + file.getName()));
            }
            Toast.makeText(inflater.getContext(), "Файлы успешно перемещены", Toast.LENGTH_SHORT).show();
            if (!isNewAlbum)
                dataImage.remove(dataImage.size() - 1);
            String dir = mFilesToMove[0].getParent();
            for (int i = 0; i < dataImage.size(); i++)
                if (dataImage.get(i).getFilePath().equals(dir)) {
                    upDateData(i, position);
                    break;
                }
        }
    }

    public boolean isSelectingDir() {
        return this.isSelectingDir;
    }

    public void cancelSelecting() {
        dataImage.remove(dataImage.size() - 1);
        this.isSelectingDir = false;
        galleryAdapter.notifyDataSetChanged();
    }

    public void createAlbum(String name) {
        temp.getImage().setImageFile(Environment.getExternalStorageDirectory() +
                getResources().getString(R.string.SAVE_FILES_PATH) +
                name);
        temp.setNewAlbum(true);
        temp.saveFiles();
        isSelectingDir = false;
    }

    public class GalleryAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public GalleryAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        @Override
        public int getCount() {
            return dataImage.size();
        }

        @Override
        public Object getItem(int position) {
            if (dataImage == null)
                return new Image();
            else
                return dataImage.get(position);
        }

        @Override
        public long getItemId(int position) {
            if (dataImage == null)
                return position;
            else
                return dataImage.get(position).getImageID();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_main_menu, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.imageMainView);
                holder.textView = (TextView) convertView.findViewById(R.id.textMainView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Image image = (Image)getItem(position);

            holder.imageView.setImageBitmap(image.getBitmap());
            if (image.getFileName() == null)
                holder.textView.setVisibility(View.INVISIBLE);
            else {
                holder.textView.setText(image.getFileDirName());
                if (holder.textView.getVisibility() == View.INVISIBLE)
                    holder.textView.setVisibility(View.VISIBLE);
            }


            return convertView;
        }


        class ViewHolder {
            public ImageView imageView;
            public TextView textView;
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String fileName);
    }
}
