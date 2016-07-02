package mobi.stolicus.imageloading;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by shtolik on 02.07.2016.
 * Class for static helper methods
 */
public class DownloadHelper {

    private static final String TAG = "DownloadHelper";

    /**
     * simple check to validate user input
     * @param query set by user
     * @return whether it starts with http/https and could be used as URL
     */
    public static boolean validate(String query) {
        return query != null && (query.startsWith("http://") || query.startsWith("https://"));
    }

    /**
     * @param s string to calculate md5 hash from
     * @return md5 hash of a given string
     */
    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest)
                hexString.append(Integer.toHexString(0xFF & aMessageDigest));

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "md5: ", e);
        }
        return "";
    }

    /**
     * Method for first checking image size and scaling it before downloading. But doesn't work
     * so using downloadImage for downloading original image
     * @param imageUrl url to image
     * @param desiredWidth width to try to scale down the image to
     * @param desiredHeight height to try to scale down the image to
     * @return bitmap of image
     */
    public static Bitmap downloadAndScaleImage(String imageUrl, int desiredWidth, int desiredHeight){
        InputStream stream = null;
        try
        {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            if (connection.getResponseCode()  != 200){
                Log.w(TAG, "downloadAndScaleImage: response:" + connection.getResponseCode()  + "/" + connection.getResponseMessage());
                return null;
            }

            //initial decoding just to find out image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 0;

            stream = connection.getInputStream();
            BufferedInputStream buffer = new BufferedInputStream(stream);
            BitmapFactory.decodeStream(buffer, null, options);
            Log.d(TAG, "downloadAndScaleImage: size =" + options.outWidth + "x" + options.outHeight);
            options.inSampleSize = calculateInSampleSize(options, desiredWidth, desiredHeight);
            Log.d(TAG, "downloadAndScaleImage: options.inSampleSize=" + options.inSampleSize);


            //need to reset or close/reopen stream otherwise getting "Android: SkImageDecoder:: Factory returned null" on emulator
//http://stackoverflow.com/questions/12006785/android-skimagedecoder-factory-returned-null
            buffer.reset();

            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(stream, null, options);

        }catch(Exception e){
            Log.e(TAG, "downloadAndScaleImage: ", e);
        }finally {
            if (stream!=null)
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.w(TAG, "downloadAndScaleImage: failed closing stream");
                }
        }
        return null;
    }

    /**
     * Method for downloading image from url and loading it to memory without scaling
     * @param imageUrl url to image
     * @return bitmap
     */
    public static Bitmap downloadImage(String imageUrl) {
        InputStream stream = null;
        try
        {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            stream = connection.getInputStream();

            if (connection.getResponseCode()  != 200){
                Log.w(TAG, "downloadAndScaleImage: response:" + connection.getResponseCode()  + "/" + connection.getResponseMessage());
                return null;
            }

            //initial decoding just to find out image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(stream, null, options);
        }catch(Exception e){
            Log.e(TAG, "downloadImage: ", e);
        }finally {
            if (stream!=null)
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e(TAG, "downloadImage: failed closing stream", e);
                }
        }
        return null;
    }


    /**
     * Method allows to find out the scale which can be applied when loading image in order to save memory
     * @param options preset and preloaded option of an image
     * @param reqWidth required width of image
     * @param reqHeight required height of image
     * @return scaling of image to be applied while loading the image (as power of 2)
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Method for loading bitmap from file on disk and scaling it if possible
     * @param absolutePath path to file to load
     * @return bitmap of image
     */
    public static Bitmap loadBitmap(String absolutePath) {

        try {
            //initial decoding just to find out image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 0;
            BitmapFactory.decodeFile(absolutePath, options);

            //checking resizing
            int min = getMinimumScreenSize();
            Log.d(TAG, "loadBitmap: size =" + options.outWidth + "x" + options.outHeight + ". Min size = " + min);
            options.inSampleSize = calculateInSampleSize(options, min, min);
            Log.d(TAG, "loadBitmap: options.inSampleSize=" + options.inSampleSize);

            //actually loading
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(absolutePath);
        }catch (Exception e){
            Log.e(TAG, "loadBitmap: ", e);
        }
        return null;
    }

    /**
     * methods for finding smaller size of device screen
     * @return smaller size of a screen to be used as maximum image size
     */
    public static int getMinimumScreenSize() {
        int height = getScreenHeight();
        int width = getScreenWidth();
        return height>width?width:height;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }
}
