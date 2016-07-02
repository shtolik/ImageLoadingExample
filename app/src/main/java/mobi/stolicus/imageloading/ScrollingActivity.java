package mobi.stolicus.imageloading;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

public class ScrollingActivity extends AppCompatActivity implements DownloadDone {
    private static final String TAG = "ScrollingActivity";

    private SearchView mSearchView;
    private ImageDownloadedReceiver mDownloadStateReceiver = null;
    private IntentFilter mStatusIntentFilter = null;
    private AppCompatImageView mImageView = null;
    private AppCompatEditText mInput = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mImageView = (AppCompatImageView) findViewById(R.id.imageView);
        mInput = (AppCompatEditText) findViewById(R.id.linkInput);
        AppCompatButton mButtonOk = (AppCompatButton) findViewById(R.id.buttonOk);
        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInput!=null) {
                    String query = mInput.getText().toString();
                    validateAndRequestDownload(query);
                }
            }
        });

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
        mSearchView = (SearchView) myActionMenuItem.getActionView();

        //making search input allow pasting link
        int searchTextViewId = mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView searchTextView = (TextView) mSearchView.findViewById(searchTextViewId);
        if (searchTextView!=null)
            searchTextView.setTextIsSelectable(true);
        else{
            EditText et = ((EditText) mSearchView.findViewById(R.id.search_src_text));
            et.setTextIsSelectable(true);
        }

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus(); //to prevent double submission of data
                if( ! mSearchView.isIconified()) {
                    mSearchView.setIconified(true);
                }

                validateAndRequestDownload(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
    }

    private void validateAndRequestDownload(String query) {
        if (DownloadHelper.validate(query)){
            updateImageView(null);
            ProcessingService.initiateDownload(getApplicationContext(), query);
        }else{
            Snackbar.make(mImageView, getApplicationContext().getString(
                    R.string.not_valid_link, query)
                    , Snackbar.LENGTH_LONG)
                    .show();
        }
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
    public void onError(final String message) {

        Runnable run = new Runnable() {
            @Override
            public void run() {
                Snackbar.make(mImageView, getApplicationContext().getString(
                        R.string.error_fetching_image, message)
                        , Snackbar.LENGTH_LONG)
                        .show();
                updateImageView(null);
            }
        };

        runOnUiThread(run);
    }

    private void updateImageView(String path){
        if (mImageView !=null && path!=null) {

            try {
                File imgFile = new File(path);

                if (imgFile.exists()) {

                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    mImageView.setImageBitmap(myBitmap);
                    mImageView.setVisibility(View.VISIBLE);
                }else{
                    mImageView.setVisibility(View.GONE);
                }
            }catch (Exception e){
                mImageView.setVisibility(View.GONE);
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
