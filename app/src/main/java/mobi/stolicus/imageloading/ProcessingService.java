package mobi.stolicus.imageloading;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class ProcessingService extends IntentService {

    public static final String DOWNLOAD_START =
            "mobi.stolicus.imageloading.ProcessingService.DOWNLOAD_START";


    public static final String DOWNLOAD_STATUS = "DOWNLOAD_STATUS";
    public static final String DOWNLOAD_FILE_PATH = "DOWNLOAD_FILE_PATH";
    public static final String DOWNLOAD_URL = "DOWNLOAD_URL";
    public static final String DOWNLOAD_STATUS_MESSAGE = "DOWNLOAD_STATUS_MESSAGE";
    private static final String TAG = "ProcessingService";

    public static final int DOWNLOAD_STATUS_FAILED_DOWNLOADING = -2;
    public static final int DOWNLOAD_STATUS_FAILED_SCALING = -3;
    public static final int DOWNLOAD_STATUS_FAILED_ROTATING = -4;
    public static final int DOWNLOAD_STATUS_FAILED_SAVING = -5;
    public static final int DOWNLOAD_STATUS_FAILED_STARTING = -1; //failed preparing link
    public static final int DOWNLOAD_STATUS_SUCCESS = 1;
    public static final int DOWNLOAD_STATUS_CACHED = 0; //file with such url was already downloaded


    public ProcessingService() {
        super("ProcessingService");
    }

    public static void initiateDownload(Context context, String query) {
        Log.i(TAG, "initiateDownload:" + query);
        Intent intent = new Intent(context, ProcessingService.class);
        intent.setAction(DOWNLOAD_START);
        intent.putExtra(DOWNLOAD_URL, query);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            final String action = intent.getAction();
            if (DOWNLOAD_START.equals(action)) {
                Log.i(TAG, "onHandleIntent: handling starting of download: " + intent.toString());
                handleDownload(intent.getStringExtra(DOWNLOAD_URL));
            } else {
                Log.d(TAG, "onHandleIntent: some other intent: " + intent.toString());
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleDownload(String url) {

        //on finishing download send broadcast
        Intent localIntent =
                new Intent(ImageDownloadedReceiver.DOWNLOAD_DONE);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);

        if (url==null || url.equals("")){
            localIntent.putExtra(DOWNLOAD_STATUS, DOWNLOAD_STATUS_FAILED_STARTING);
        }else {

            String filename = DownloadHelper.md5(url);

            //Saving to cache directory of the app on external storage if available, or internal memory if not.
            File file = getApplicationContext().getExternalCacheDir();
            if (file==null) {
                file = getApplicationContext().getCacheDir();
            }

            String downloadedFilePath = file.getPath();
            downloadedFilePath += "/" + filename + ".jpg";

            Log.d(TAG, "handleDownload: will save to " + downloadedFilePath);

            if (checkFileExist(downloadedFilePath)){
                Log.i(TAG, "handleDownload: file already cached " + downloadedFilePath);
                localIntent.putExtra(DOWNLOAD_STATUS, DOWNLOAD_STATUS_CACHED);
            }else {

                int result = startDownload(url, downloadedFilePath);
                localIntent.putExtra(DOWNLOAD_STATUS, result);
            }
            localIntent.putExtra(DOWNLOAD_FILE_PATH, downloadedFilePath);
        }

        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private boolean checkFileExist(String path) {
        File file = new File(path);
        return file.exists() && file.length() > 0;
    }

    private int startDownload(String url, String downloadedFilePath) {
        int minimumSize = DownloadHelper.getMinimumScreenSize();

        Bitmap bitmap = DownloadHelper.downloadAndScaleImage(url, minimumSize, minimumSize);

        if (bitmap==null){
            return DOWNLOAD_STATUS_FAILED_DOWNLOADING;
        }else{
            Log.d(TAG, "startDownload: download ok.");
        }
        bitmap = rotateBitmap(bitmap);
        if (bitmap==null){
            return DOWNLOAD_STATUS_FAILED_ROTATING;
        }else{
            Log.d(TAG, "startDownload: rotated ok");
        }
        int res = saveBitmap(bitmap, downloadedFilePath);
        Log.d(TAG, "startDownload: saved " + res + "/ to " + downloadedFilePath);
        return res;
    }

    private Bitmap rotateBitmap(Bitmap bitmap) {
        try {
            Matrix m = new Matrix();
            m.postRotate(180);
            Bitmap newBm =  Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            //recycling old bitmap
            bitmap.recycle();
            return newBm;
        }catch (Exception e){
            Log.e(TAG, "rotateBitmap: ", e);
        }
        return null;
    }

    private int saveBitmap(Bitmap bitmap, String downloadedFilePath) {
        File file = new File (downloadedFilePath);
        if (file.exists()) {
            if (file.delete()){
                Log.w(TAG, "saveBitmap: failed to delete file before saving." + downloadedFilePath);
            }
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return DOWNLOAD_STATUS_SUCCESS;
        } catch (Exception e) {
            Log.e(TAG, "saveBitmap: ", e);
        }
        return DOWNLOAD_STATUS_FAILED_SAVING;
    }

}
