package adv.android_11.solleks.homework2;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements
            MainFragment.OnFragmentInteractionListener,
            GalleryFragment.OnDetailFragmentListener{

    private MainFragment mainFragment;
    private GalleryFragment galleryFragment;
    private DetailFragment detailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainFragment = new MainFragment();
        galleryFragment = new GalleryFragment();
        detailFragment = new DetailFragment();

        GalleryFragment.IMAGE_WIDTH = GalleryFragment.IMAGE_HEIGHT = 100;

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragmentContainer, mainFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String fileName) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.replace(R.id.fragmentContainer, galleryFragment);
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
        galleryFragment.changePath(fileName);
    }

    @Override
    public void onDetailFragmentOpener(String fileName) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.replace(R.id.fragmentContainer, detailFragment);
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
        detailFragment.setImageFile(fileName);
    }
}
