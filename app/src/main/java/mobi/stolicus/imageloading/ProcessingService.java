package mobi.stolicus.imageloading;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ProcessingService extends IntentService {

    public static final String DOWNLOAD_START =
            "mobi.stolicus.imageloading.ProcessingService.DOWNLOAD_START";


    public static final String DOWNLOAD_STATUS = "DOWNLOAD_STATUS";
    public static final String DOWNLOAD_FILE_PATH = "DOWNLOAD_FILE_PATH";
    public static final String DOWNLOAD_STATUS_MESSAGE = "DOWNLOAD_STATUS_MESSAGE";
    private static final String TAG = "ProcessingService";

    public static final int DOWNLOAD_STATUS_FAILED = -2; //failed downloading
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
        intent.setData(Uri.parse(query));
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            final String action = intent.getAction();
            if (DOWNLOAD_START.equals(action)) {
                Log.i(TAG, "onHandleIntent: handling starting of download: " + intent.toString());
                final Uri param1 = intent.getData();
                handleDownload(param1);
            } else {
                Log.d(TAG, "onHandleIntent: some other intent: " + intent.toString());
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleDownload(Uri uri) {

        //on finishing download send broadcast
        Intent localIntent =
                new Intent(ImageDownloadedReceiver.DOWNLOAD_DONE);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);

        if (uri==null || uri.equals(Uri.EMPTY)){
            localIntent.putExtra(DOWNLOAD_STATUS, DOWNLOAD_STATUS_FAILED_STARTING);
        }else {

            String filename = md5(uri.toString());
            //TODO: check that external storage is available and save there
            String downloadedFilePath = getApplicationContext().getFilesDir().getPath();
            downloadedFilePath += "/" + filename + ".jpg";

            if (checkFileExist(downloadedFilePath)){
                localIntent.putExtra(DOWNLOAD_STATUS, DOWNLOAD_STATUS_CACHED);
            }else {

                int result = startDownload(uri, downloadedFilePath);
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

    private int startDownload(Uri uri, String downloadedFilePath) {
        //TODO: return DOWNLOAD_STATUS_SUCCESS;

        return DOWNLOAD_STATUS_FAILED;
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest)
                hexString.append(Integer.toHexString(0xFF & aMessageDigest));

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
