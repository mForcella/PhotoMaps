package edu.newpaltz.photomaps;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.IOException;
import java.util.ArrayList;

public class MyApplication extends Application {

    public static String mJpg;
    public static String mImagePath;
    public static String mPhotoPath;
    public static final int REQUEST_IMAGE_CAPTURE = 1111; // image capture value
    public static Context context; // to get the application context
    public static String attachUrl = "http://cs.newpaltz.edu/~forcel96/crosscountry/uploadAttach.php";
    public static String postPhotoUrl = "http://cs.newpaltz.edu/~forcel96/crosscountry/postPhoto.php";
    public static ArrayList<Photo> mListPhotos;
    public static int mPages;

    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
        mImagePath = Environment.getExternalStorageDirectory() + "/";
        getPhotos();
    }

    public static void getPhotos() {
        // get list of photos
        try {
            DBAdapter db = new DBAdapter(context);
            mListPhotos = db.getPhotos();
            mPages = mListPhotos.size();
            db.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    // creates a thumbnail from an image path
    public static Bitmap getThumbnailBitmap(String path, int thumbnailSize) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
            return null;
        int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
                : bounds.outWidth;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = originalSize / thumbnailSize;
        return BitmapFactory.decodeFile(path, opts);
    }
}
