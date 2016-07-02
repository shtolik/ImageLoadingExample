package mobi.stolicus.imageloading;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

public class ScrollingActivity extends AppCompatActivity implements DownloadDone {
    private static final String TAG = "ScrollingActivity";

    private SearchView searchView;
    private ImageDownloadedReceiver mDownloadStateReceiver = null;
    private IntentFilter mStatusIntentFilter = null;
    private AppCompatImageView imageView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        imageView = (AppCompatImageView) findViewById(R.id.imageView);

        // The filter's action is DOWNLOAD_START
        mStatusIntentFilter = new IntentFilter(
                ImageDownloadedReceiver.DOWNLOAD_DONE);
        mStatusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        // Instantiates a new DownloadStateReceiver
        mDownloadStateReceiver =
                new ImageDownloadedReceiver(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDownloadStateReceiver!=null && mStatusIntentFilter !=null)
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mDownloadStateReceiver, mStatusIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDownloadStateReceiver!=null)
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mDownloadStateReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);

        final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus(); //to prevent double submission of data
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
//                myActionMenuItem.collapseActionView();

                if (RequestHelper.validate(query)){
                    Snackbar.make(searchView, getApplicationContext().getString(
                                R.string.starting_image_loading, query),
                            Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                    ProcessingService.initiateDownload(getApplicationContext(), query);
                }else{
                    Snackbar.make(searchView, getApplicationContext().getString(
                                R.string.not_valid_link, query)
                            , Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
    }

    @Override
    public void onImageLoaded(final String path) {

        Runnable run = new Runnable() {
            @Override
            public void run() {

                updateImageView(path);
            }
        };

        runOnUiThread(run);
    }

    @Override
    public void onError(int status, final String message) {

        Runnable run = new Runnable() {
            @Override
            public void run() {
                Snackbar.make(searchView, getApplicationContext().getString(
                        R.string.error_downloading, message)
                        , Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                updateImageView(null);
            }
        };

        runOnUiThread(run);
    }

    private void updateImageView(String path){
        if (imageView!=null) {

            try {
                File imgFile = new File(path);

                if (imgFile.exists()) {

                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                    imageView.setVisibility(View.VISIBLE);
                }else{
                    imageView.setVisibility(View.GONE);
                }
            }catch (Exception e){
                imageView.setVisibility(View.GONE);
                Log.e(TAG, "updateImageView: ", e);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
