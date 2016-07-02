package mobi.stolicus.imageloading;

/**
 * Created by shtolik on 02.07.2016.
 */
public class RequestHelper {

    public static boolean validate(String query){
        if (query.startsWith("http://") || query.startsWith("https://")){
            return true;
        }else{
            return false;
        }
    }

}
