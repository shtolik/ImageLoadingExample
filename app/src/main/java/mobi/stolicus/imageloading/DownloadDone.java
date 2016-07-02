package mobi.stolicus.imageloading;

/**
 * Created by shtolik on 02.07.2016.
 * interface for passing results to activity
 */
interface DownloadDone {
    void onImageLoaded(String path);
    void onError(String message);
}
