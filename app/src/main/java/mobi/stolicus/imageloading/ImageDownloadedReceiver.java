package mobi.stolicus.imageloading;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ImageDownloadedReceiver extends BroadcastReceiver {

    private static final String TAG = "ImageDownloadedReceiver";
    private DownloadDone callback = null;

    public static final String DOWNLOAD_DONE =
            "mobi.stolicus.imageloading.ImageDownloadedReceiver.DOWNLOAD_DONE";
    public ImageDownloadedReceiver() {
    }

    public ImageDownloadedReceiver(DownloadDone callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (callback==null) {
            Log.w(TAG, "callback not initialized yet but got a broadcast. onReceive: " + intent.toString());
            return;
        }

        Log.i(TAG, "onReceive: " + intent.toString());
        if (intent.getAction().equals(DOWNLOAD_DONE)){
            int result = intent.getIntExtra(ProcessingService.DOWNLOAD_STATUS, -1);
            String message = intent.getStringExtra(ProcessingService.DOWNLOAD_STATUS_MESSAGE);
            if (result>=0) {
                callback.onImageLoaded(intent.getStringExtra(ProcessingService.DOWNLOAD_FILE_PATH));
            }else{
                callback.onError(result, message);
            }
        }

    }
}
