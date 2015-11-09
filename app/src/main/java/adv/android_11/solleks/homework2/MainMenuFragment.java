package adv.android_11.solleks.homework2;

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
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Created by Константин on 07.11.2015.
 *
 */
public class MainMenuFragment extends Fragment {

    private ArrayList<String> data;
    private Image[] dataImage;
    private File file;
    private GalleryAdapter galleryAdapter;

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
        if (imageList.length > 0) {
            data.add(imageList[imageList.length - 1].getAbsolutePath());
        }
        for (File dir : dirList) {
            String subDirs[] = dir.getAbsolutePath().split("/");
            boolean contains = false;
            for (int i = 0; i < subDirs.length; i++)
                if (subDirs[i].equals("Music")) {
                    contains = true;
                    break;
                }
            if (!contains)
            data.addAll(saveFilesFromDir(dir));
        }

        return data;
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
                Toast.makeText(inflater.getContext(), ((Image)galleryAdapter.getItem(position)).getFile(), Toast.LENGTH_SHORT).show();
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
                convertView = mInflater.inflate(R.layout.list_item_main, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Image image = (Image)getItem(position);
            try {
                holder.imageView.setImageBitmap(image.getBitmap());
            } catch (Exception e) {
                holder.imageView.setImageBitmap(null);
            }

            return convertView;
        }


        class ViewHolder {
            public ImageView imageView;
        }
    }
}
