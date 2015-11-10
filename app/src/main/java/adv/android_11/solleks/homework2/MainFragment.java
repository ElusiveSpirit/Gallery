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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Константин on 07.11.2015.
 *
 */
public class MainFragment extends Fragment {

    public static int IMAGE_WIDTH;
    public static int IMAGE_HEIGHT;

    private ArrayList<String> data;
    private Image[] dataImage;
    private File file;
    private GalleryAdapter galleryAdapter;

    private OnFragmentInteractionListener mListener;


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        File rootSD = Environment.getExternalStorageDirectory();
        file = rootSD;
        data = saveFilesFromDir(rootSD);
        dataImage = new Image[data.size()];
        for (int i = 0; i < data.size(); i++) {
            dataImage[i] = new Image(data.get(i), i, null);
            dataImage[i].setDimens(IMAGE_WIDTH, IMAGE_HEIGHT);
        }
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.execute(rootSD);
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

    public ArrayList<String> saveFilesFromDir(File rootDir) {
        ArrayList<String> data = new ArrayList<>();

        File[] dirList = rootDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (!name.contains("."));
            }
        });
        File[] imageList = rootDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"));
            }
        });

        ArrayList<File> fileList = new ArrayList<>();
        fileList.addAll(Arrays.asList(imageList));
        fileList = sort(fileList);
        if (fileList.size() > 0) {
            data.add(fileList.get(fileList.size() - 1).getAbsolutePath());
        }
        for (File dir : dirList) {
            String subDirs[] = dir.getAbsolutePath().split("/");
            boolean contains = false;
            for (String subDir : subDirs)
                if (subDir.equals("Music")) {
                    contains = true;
                    break;
                }
            if (!contains)
            data.addAll(saveFilesFromDir(dir));
        }

        return data;
    }

    public ArrayList<File> sort(ArrayList<File> input) {
        if (input.size() == 0) {
            return input;
        }
        File head = input.get(0);
        input.remove(0);
        ArrayList<File> right = new ArrayList<>(input.size()/2);
        ArrayList<File> left = new ArrayList<>(input.size()/2);

        for(File element: input) {
            if (element.lastModified() > head.lastModified()) {
                right.add(element);
            } else {
                left.add(element);
            }
        }

        ArrayList<File> result = new ArrayList<>(input.size());
        result.addAll(sort(left));
        result.add(head);
        result.addAll(sort(right));
        return result;
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
                openGalleryFragment(((Image) galleryAdapter.getItem(position)).getFilePath());
               // Toast.makeText(inflater.getContext(), ((Image)galleryAdapter.getItem(position)).getFile(), Toast.LENGTH_SHORT).show();
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
                holder.textView.setText(image.getFileDir());
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
        public void onFragmentInteraction(String fileName);
    }
}
