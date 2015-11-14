package adv.android_11.solleks.homework2;

import android.content.Context;
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

    private ArrayList<String> data;
    private Image[] dataImage;
    private GalleryAdapter galleryAdapter;

    private boolean mSelectDirToMove;
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
        mSelectDirToMove = true;
        mFilesToMove = files;
    }

    public void setData(ArrayList<String> data) {
        this.data = data;
        File rootSD = Environment.getExternalStorageDirectory();

        if (data != null) {
            dataImage = new Image[data.size()];
            for (int i = 0; i < data.size(); i++) {
                dataImage[i] = new Image(data.get(i), i, null);
                dataImage[i].setDimens(IMAGE_WIDTH, IMAGE_HEIGHT);
            }
        } else {
            dataImage = new Image[0];
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

    public void upDateData() {
        // TODO Выполнять перезагрузку только для двух папок
        dataImage = new Image[0];
        galleryAdapter.notifyDataSetChanged();

        mListener.upDateFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mSelectDirToMove = false;

        data = getArguments().getStringArrayList(DATA_KEY);
        setData(data);
    }

    class ImageLoader extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... params) {
            for (int i = 0; i < data.size(); i++) {
                dataImage[i].run();
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
    }



    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView)view.findViewById(R.id.menuGridView);

        galleryAdapter = new GalleryAdapter(inflater);
        gridView.setAdapter(galleryAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mSelectDirToMove) {
                    mSelectDirToMove = false;
                    Image image = ((Image) galleryAdapter.getItem(position));
                    for (File file : mFilesToMove) {
                        file.renameTo(new File(image.getFilePath() + "/" + file.getName()));
                    }
                    Toast.makeText(inflater.getContext(), "Файлы успешно перемещены", Toast.LENGTH_SHORT).show();
                    upDateData();
                } else
                    openGalleryFragment(((Image) galleryAdapter.getItem(position)).getFilePath());
            }
        });
        return view;
    }

    public class GalleryAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public GalleryAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        /*    ImageLoader imageLoader = new ImageLoader();
            imageLoader.execute(file);*/
        }

        @Override
        public int getCount() {
            return dataImage.length;
        }

        @Override
        public Object getItem(int position) {
            if (dataImage == null)
                return new Image();
            else
                return dataImage[position];
        }

        @Override
        public long getItemId(int position) {
            if (dataImage == null)
                return position;
            else
                return dataImage[position].getImageID();
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
            try {
                holder.imageView.setImageBitmap(image.getBitmap());
                holder.textView.setText(image.getFileDirName());
            } catch (Exception e) {
                holder.imageView.setImageBitmap(null);
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
        void upDateFragment();
    }
}
