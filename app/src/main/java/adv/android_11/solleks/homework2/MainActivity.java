package adv.android_11.solleks.homework2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements
            MainFragment.OnFragmentInteractionListener,
            GalleryFragment.OnGalleryFragmentListener,
            DetailFragment.OnDetailFragmentListener,
            NewAlbumFragment.OnAlertFragmentListener,
            FragmentManager.OnBackStackChangedListener{

    private MainFragment mainFragment;
    private GalleryFragment galleryFragment;
    private DetailFragment detailFragment;

    private boolean upDateGalleryFragmentData;

    private ActionBar mActionBar;

    private static int DisplayWidth;
    private static int DisplayHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();

        FragmentManager manager = getSupportFragmentManager();
        manager.addOnBackStackChangedListener(this);
        changeActionBar();

        List<Fragment> fragments = manager.getFragments();


        if (fragments == null) {
            MainFragment.IMAGE_HEIGHT = MainFragment.IMAGE_WIDTH = getResources().getDimensionPixelOffset(R.dimen.preview_image_height);

            DirLoader loader = new DirLoader();
            loader.execute(Environment.getExternalStorageDirectory());
        } else {
            FragmentTransaction fragmentTransaction = manager.beginTransaction();
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragmentTransaction.attach(fragment);
                    switch (fragment.getTag()) {
                        case MainFragment.TAG:
                            mainFragment = (MainFragment) fragment;
                            break;
                        case GalleryFragment.TAG:
                            galleryFragment = (GalleryFragment) fragment;
                            break;
                        case DetailFragment.TAG:
                            detailFragment = (DetailFragment) fragment;
                    }
                }
            }
            fragmentTransaction.commit();
        }
        if (galleryFragment == null) {
            galleryFragment = new GalleryFragment();
            GalleryFragment.IMAGE_WIDTH = GalleryFragment.IMAGE_HEIGHT = getResources().getDimensionPixelSize(R.dimen.image_height);
        }
        if (detailFragment == null)
            detailFragment = new DetailFragment();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        ((Toolbar) findViewById(R.id.toolbar)).getMenu().findItem(R.id.action_move).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings :
                NewAlbumFragment albumFragment = new NewAlbumFragment();
                albumFragment.show(getSupportFragmentManager(), "tag1");
                Toast.makeText(MainActivity.this, "Toast", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, albumFragment.getText(), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_move :
                moveItem();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String fileName) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (getSupportFragmentManager().findFragmentByTag(GalleryFragment.TAG) == null) {
            fragmentTransaction
                    .replace(R.id.fragmentContainer, galleryFragment, GalleryFragment.TAG)
                    .addToBackStack(null)
                    .commit();
        } else {
            fragmentTransaction
                    .hide(mainFragment)
                    .show(galleryFragment)
                    .addToBackStack(null)
                    .commit();
        }
        if (galleryFragment.getOpenedDir() == null ||
                !galleryFragment.getOpenedDir().equals(fileName) ||
                upDateGalleryFragmentData)
            galleryFragment.changePath(fileName);
        if (upDateGalleryFragmentData)
            upDateGalleryFragmentData = false;
    }


    @Override
    public void onDetailFragmentOpener(String fileName) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (getSupportFragmentManager().findFragmentByTag(DetailFragment.TAG) == null) {
            fragmentTransaction
                    .replace(R.id.fragmentContainer, detailFragment, DetailFragment.TAG)
                    .addToBackStack(null)
                    .commit();
        } else {
            fragmentTransaction
                    .hide(galleryFragment)
                    .show(detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
        DisplayHeight = findViewById(R.id.fragmentContainer).getMeasuredHeight();
        DisplayWidth = findViewById(R.id.fragmentContainer).getMeasuredWidth();
        detailFragment.setImageFile(fileName);
    }

    @Override
    public void showMoveButton(boolean show) {
        ((Toolbar)findViewById(R.id.toolbar)).getMenu().findItem(R.id.action_move).setVisible(show);
    }

    public void moveItem() {
        File[] files = galleryFragment.getSelectedFiles();
        galleryFragment.cancelSelecting();
        mainFragment.setSelectDirToMove(files);
        upDateGalleryFragmentData = true;
        onBackPressed();
    }

    public static int getDisplayWidth() {
        return DisplayWidth;
    }
    public static int getDisplayHeight() {
        return DisplayHeight;
    }

    @Override
    public void onBackStackChanged() {
        changeActionBar();
    }

    public void changeActionBar(){
        //Enable Up button only  if there are entries in the back stack
        boolean canBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
        if (mActionBar != null && mainFragment != null) {
            if (mainFragment.isHidden())
                mActionBar.setDisplayHomeAsUpEnabled(canBack);
            else {
                ((Toolbar) findViewById(R.id.toolbar)).getMenu().findItem(R.id.action_move).setVisible(false);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    public void setActionBarDisplay(boolean show) {
        ((Toolbar)findViewById(R.id.toolbar)).setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void newAlbum(String albumName) {
        mainFragment.createAlbum(albumName);
    }

    class DirLoader extends AsyncTask<File, Image, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(File... params) {

            File rootDir = params[0];

            publishProgress(new Image());
            return saveFilesFromDir(rootDir);
        }

        @Override
        protected void onProgressUpdate(Image... values) {
            super.onProgressUpdate(values);
            Toast.makeText(MainActivity.this, "Загрузка файлов", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);

            if (mainFragment == null) {
                mainFragment = MainFragment.newInstance(strings);

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction
                        .add(R.id.fragmentContainer, mainFragment, MainFragment.TAG)
                        .add(R.id.fragmentContainer, galleryFragment, GalleryFragment.TAG)
                        .add(R.id.fragmentContainer, detailFragment, DetailFragment.TAG)
                        .hide(galleryFragment)
                        .hide(detailFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                mainFragment.setData(strings);
            }
        }
    }

    @Override
    public void onBackPressed() {
        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();

        if (manager.findFragmentByTag(MainFragment.TAG).isVisible()) {
            if (mainFragment.isSelectingDir()) {
                mainFragment.cancelSelecting();
                return;
            }
            finish();
        } else if (manager.findFragmentByTag(GalleryFragment.TAG).isVisible()) {
            if (galleryFragment.isIsNowSelecting()) {
                galleryFragment.cancelSelecting();
                return;
            }
            galleryFragment.cancelLoading();
        } else if (manager.findFragmentByTag(DetailFragment.TAG).isVisible()) {
            detailFragment.cancelLoading();
        }

        super.onBackPressed();
    }

    public static ArrayList<String> saveFilesFromDir(File rootDir) {
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

    public static ArrayList<File> sort(ArrayList<File> input) {
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
}
