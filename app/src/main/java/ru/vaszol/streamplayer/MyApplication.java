package ru.vaszol.streamplayer;

import android.app.Application;
import android.content.Context;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

/**
 * Created by vaszol on 06.05.2016.
 */
public class MyApplication extends Application {

    private static MyApplication instance;
    public final static String vidAddress1 = "rtmp://rian.cdnvideo.ru:1935/rr/stream20";
    public final static String vidAddress2 = "sdcard/Download/bootanimation_nexus.mp4";

    public final static String TAG = "MyApplication";
    private static SimpleArrayMap<String, Object> sDataMap = new SimpleArrayMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "onCreate MyApp");
        instance = this;
    }

    /**
     * Called when the overall system is running low on memory
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "System is running low on memory");

//        BitmapCache.getInstance().clear();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.w(TAG, "onTrimMemory, level: "+level);

//        BitmapCache.getInstance().clear();
    }

    /**
     * @return the main context of the Application
     */
    public static Context getAppContext()
    {
        return instance;
    }


}
