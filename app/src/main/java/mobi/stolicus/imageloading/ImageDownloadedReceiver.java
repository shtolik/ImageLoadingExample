package mobi.stolicus.imageloading;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * broadcast receiver for passing result of file downloading from intent service to activity
 */
public class ImageDownloadedReceiver extends BroadcastReceiver {

    private static final String TAG = "ImageDownloadedReceiver";
    private DownloadDone callback = null;

    public static final String DOWNLOAD_DONE =
            "mobi.stolicus.imageloading.ImageDownloadedReceiver.DOWNLOAD_DONE";

    /**
     * need an empty constructor as well
     */
    @SuppressWarnings("unused")
    public ImageDownloadedReceiver() {
    }

    /**
     * @param callback interface for passing results to activity
     */
    public ImageDownloadedReceiver(DownloadDone callback) {
        this.callback = callback;
    }

    /**
     * @param context context
     * @param intent intent with download status and path to file
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        if (callback==null) {
            Log.w(TAG, "callback not initialized yet but got a broadcast. onReceive: " + intent.toString());
            return;
        }

        Log.i(TAG, "onReceive: " + intent.toString());
        if (intent.getAction().equals(DOWNLOAD_DONE)){
            int result = intent.getIntExtra(ProcessingService.DOWNLOAD_STATUS, -1);
            if (result>=0) {
                callback.onImageLoaded(intent.getStringExtra(ProcessingService.DOWNLOAD_FILE_PATH));
            }else{
                String message = getMessageFromError(context, result);
                callback.onError(message);
            }
        }
    }

    public static String getMessageFromError(Context context, int result) {
        switch (result){
            default:
            case ProcessingService.DOWNLOAD_STATUS_FAILED_DOWNLOADING:
                return context.getString(R.string.failed_downloading);
            case ProcessingService.DOWNLOAD_STATUS_FAILED_SAVING:
                return context.getString(R.string.failed_saving);
            case ProcessingService.DOWNLOAD_STATUS_FAILED_ROTATING:
                return context.getString(R.string.failed_rotating);
            case ProcessingService.DOWNLOAD_STATUS_FAILED_STARTING:
                return context.getString(R.string.failed_starting);
        }
    }
}
