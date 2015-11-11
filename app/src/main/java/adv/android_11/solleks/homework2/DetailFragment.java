package adv.android_11.solleks.homework2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * Created by Константин on 10.11.2015.
 *
 */
public class DetailFragment extends Fragment {

    public static final String TAG = "adv.android_11.solleks.homework2.DetailFragment";

    private ImageView mImageView;
    private Loader loader;
    private Image image;

    public void setImageFile(String imageFile) {
        loader = new Loader();
        loader.execute(imageFile);
    }

    class Loader extends AsyncTask<String, Void, Image> {
        @Override
        protected Image doInBackground(String... params) {
            image = new Image(params[0]);
            image.setDimens(MainActivity.getDisplayWidth(), MainActivity.getDisplayHeight());
            image.run();
            while (image.isAlive())
                Thread.yield();
            return image;
        }

        @Override
        protected void onPostExecute(Image image) {
            super.onPostExecute(image);
            mImageView.setImageBitmap(image.getBitmap());
        }
    }

    public void cancelLoading() {
        loader.cancel(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        mImageView = (ImageView) view.findViewById(R.id.imageViewDetail);
        if (image != null)
            mImageView.setImageBitmap(image.getBitmap());

        return view;
    }


}