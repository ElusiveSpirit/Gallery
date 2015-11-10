package adv.android_11.solleks.homework2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by Константин on 10.11.2015.
 *
 */
public class DetailFragment extends Fragment {

    private File imageFile;
    private ImageView mImageView;
    private Loader loader;

    private int width;
    private int height;

    public void setImageFile(String imageFile) {
        loader = new Loader();
        loader.execute(imageFile);
    }

    class Loader extends AsyncTask<String, Void, Image> {
        @Override
        protected Image doInBackground(String... params) {
            Image image = new Image(params[0]);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        mImageView = (ImageView) view.findViewById(R.id.imageViewDetail);
        this.width = mImageView.getWidth();
        this.height = mImageView.getHeight();

        //this.width =

        return view;
    }


}
