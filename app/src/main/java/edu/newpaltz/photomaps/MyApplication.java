package edu.newpaltz.photomaps;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class MyApplication extends Application {

    public static String mJpg;
    public static String mImagePath;
    public static String mPhotoPath;
    public static final int REQUEST_IMAGE_CAPTURE = 1111; // image capture value
    public static Context context; // to get the application context
    public static String attachUrl = "http://cs.newpaltz.edu/~forcel96/surveyit/uploadAttach.php";

    public void onCreate(){
        super.onCreate();
        MyApplication.context = getApplicationContext();
        MyApplication.mImagePath = Environment.getExternalStorageDirectory() + "/";
        Log.i("imagePath",mImagePath);
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
