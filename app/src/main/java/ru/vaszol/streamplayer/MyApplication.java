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
    public final static String vidAddress2 = "rtmp://178.249.242.111:1935/live/tv6";    //ТНТ (Курск)
    public final static String vidAddress3 = "rtmp://yellow.ether.tv/live/cheljabinsk/broadcast4";    //ЮУрГУ ТВ (Челябинск)

    public final static String vidAddress4 = "http://hlstv.kuz.211.ru/239.211.4.7/stream.m3u8"; //матч тв hls
    public final static String vidAddress5 = "http://msk3.peers.tv/streaming/sport/126/tvrecw/playlist.m3u8";
    public final static String vidAddress6 = "http://wse.planeta-online.tv:1935/live/channel_4/index.m3u8";    //Fresh TV
    public final static String vidAddress7 = "http://yellow.ether.tv:1935/live/voronezh/broadcast37/chunklist.m3u8";    //Воронеж
//    public final static String vidAddress2 = "sdcard/Download/bootanimation_nexus.mp4"; //local video (media without uri.parse!!)

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
        Log.w(TAG, "onTrimMemory, level: " + level);

//        BitmapCache.getInstance().clear();
    }

    /**
     * @return the main context of the Application
     */
    public static Context getAppContext() {
        return instance;
    }


}
