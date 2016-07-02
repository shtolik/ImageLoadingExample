package mobi.stolicus.imageloading;

import android.graphics.BitmapFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * unit tests to check helper methods
 * Created by shtolik on 02.07.2016.
 */
public class DownloadHelperTest {

    @Test
    public void testCalculateInSampleSize() throws Exception {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outWidth = 1800;
        options.outHeight = 1800;
        assertEquals(2, DownloadHelper.calculateInSampleSize(options, 800, 800));

        options.outWidth = 1610;
        options.outHeight = 1610;
        assertEquals(2, DownloadHelper.calculateInSampleSize(options, 800, 800));

        options.outWidth = 1600;
        options.outHeight = 1600;
        assertEquals(1, DownloadHelper.calculateInSampleSize(options, 800, 800));

        options.outWidth = 1500;
        options.outHeight = 1500;
        assertEquals(1, DownloadHelper.calculateInSampleSize(options, 800, 800));

        options.outWidth =700;
        options.outHeight = 700;
        assertEquals(1, DownloadHelper.calculateInSampleSize(options, 800, 800));
    }


    @Test
    public void testValidate() throws Exception {
        assertEquals(false, DownloadHelper.validate(""));
        assertEquals(true, DownloadHelper.validate("https://asdassas"));
        assertEquals(true, DownloadHelper.validate("http://asdassas"));
        assertEquals(false, DownloadHelper.validate(" http://asdassas"));
        assertEquals(false, DownloadHelper.validate("asdasa"));
        assertEquals(false, DownloadHelper.validate(null));

    }
}