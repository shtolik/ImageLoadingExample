package mobi.stolicus.imageloading;

/**
 * Created by shtolik on 02.07.2016.
 */
public interface DownloadDone {
    void onImageLoaded(String path);
    void onError(int status, String message);
}
