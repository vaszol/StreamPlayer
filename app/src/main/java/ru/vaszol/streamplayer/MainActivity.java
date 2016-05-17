package ru.vaszol.streamplayer;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements
        IVLCVout.Callback,
        MediaPlayer.EventListener {

    private String mVideoUrl;

    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_HORIZONTAL = 1;
    private static final int SURFACE_FIT_VERTICAL = 2;
    private static final int SURFACE_FILL = 3;
    private static final int SURFACE_16_9 = 4;
    private static final int SURFACE_4_3 = 5;
    private static final int SURFACE_ORIGINAL = 6;
    private int CURRENT_SIZE = SURFACE_BEST_FIT;

    private SurfaceView mSurfaceView = null;
    private FrameLayout mSurfaceFrame = null;
    private ProgressBar progressBar = null;

    private final Handler mHandler = new Handler();
    private View.OnLayoutChangeListener mOnLayoutChangeListener = null;

    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private IVLCVout ivlcVout = null;
    private int mVideoHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoVisibleHeight = 0;
    private int mVideoVisibleWidth = 0;
    private int mVideoSarNum = 0;
    private int mVideoSarDen = 0;

    long i = 0;

    /**
     * Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // SETUP THE UI
        setContentView(R.layout.activity_main);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mVideoUrl = MyApplication.vidAddress1;

        mSurfaceView = (SurfaceView) findViewById(R.id.player_surface);
        mSurfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");   // verbosity
        args.add("--width=640");
        args.add("--height=480");
        args.add("--noaudio");
        mLibVLC = new LibVLC(args);
        mMediaPlayer = new MediaPlayer(mLibVLC);
        mMediaPlayer.setEventListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateVideoSurfaces();
    }

    @Override
    protected void onResume() {
        super.onResume();
        createMediaPlayer();
        playMediaPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyPlayer();
    }

    /**
     * Listebers
     */
    public void onClick(View view) {
        Log.d(MyApplication.TAG, "onClick=" + view.toString());
        if (view.getId() == R.id.rtmp) {
            switch (mVideoUrl) {
                case MyApplication.vidAddress1:
                    mVideoUrl = MyApplication.vidAddress2;
                    break;
                case MyApplication.vidAddress2:
                    mVideoUrl = MyApplication.vidAddress3;
                    break;
                case MyApplication.vidAddress3:
                    mVideoUrl = MyApplication.vidAddress1;
                    break;
                default:
                    mVideoUrl = MyApplication.vidAddress1;
            }
            Toast.makeText(this, "mVideoUrl=" + mVideoUrl, Toast.LENGTH_SHORT).show();
            playMediaPlayer();
        }
        if (view.getId() == R.id.hls) {
            switch (mVideoUrl) {
                case MyApplication.vidAddress4:
                    mVideoUrl = MyApplication.vidAddress5;
                    break;
                case MyApplication.vidAddress5:
                    mVideoUrl = MyApplication.vidAddress6;
                    break;
                case MyApplication.vidAddress6:
                    mVideoUrl = MyApplication.vidAddress7;
                    break;
                case MyApplication.vidAddress7:
                    mVideoUrl = MyApplication.vidAddress4;
                    break;
                default:
                    mVideoUrl = MyApplication.vidAddress4;
            }
            Toast.makeText(this, "mVideoUrl=" + mVideoUrl, Toast.LENGTH_SHORT).show();
            playMediaPlayer();
        }
        if (view.getId() == R.id.clear) {
            Log.d(MyApplication.TAG, "clear");
            stopPlayer();
            createMediaPlayer();
            playMediaPlayer();
        }
        if (view.getId() == R.id.player_surface) {
            String mode = "";
            switch (CURRENT_SIZE) {
                case SURFACE_BEST_FIT:          //0
                    CURRENT_SIZE = SURFACE_FIT_HORIZONTAL;
                    mode = "SURFACE_FIT_HORIZONTAL";
                    break;
                case SURFACE_FIT_HORIZONTAL:    //1
                    CURRENT_SIZE = SURFACE_FIT_VERTICAL;
                    mode = "SURFACE_FIT_VERTICAL";
                    break;
                case SURFACE_FIT_VERTICAL:      //2
                    CURRENT_SIZE = SURFACE_FILL;
                    mode = "SURFACE_FILL";
                    break;
                case SURFACE_FILL:              //3
                    CURRENT_SIZE = SURFACE_16_9;
                    mode = "SURFACE_16_9";
                    break;
                case SURFACE_16_9:              //4
                    CURRENT_SIZE = SURFACE_4_3;
                    mode = "SURFACE_4_3";
                    break;
                case SURFACE_4_3:               //5
                    CURRENT_SIZE = SURFACE_ORIGINAL;
                    mode = "SURFACE_ORIGINAL";
                    break;
                case SURFACE_ORIGINAL:          //6
                    CURRENT_SIZE = SURFACE_BEST_FIT;
                    mode = "SURFACE_BEST_FIT";
                    break;
            }
            updateVideoSurfaces();
            Toast.makeText(this, "CURRENT_SIZE=" + CURRENT_SIZE + " " + mode, Toast.LENGTH_SHORT).show();
        }
        Log.d(MyApplication.TAG, "path=" + mVideoUrl);

    }

    private void updateVideoSurfaces() {
        if (mVideoWidth * mVideoHeight == 0)
            return;
        int sw = getWindow().getDecorView().getWidth();
        int sh = getWindow().getDecorView().getHeight();

//        mMediaPlayer.getVLCVout().setWindowSize(sw, sh);
        ivlcVout.setWindowSize(sw, sh);
        double dw = sw, dh = sh;
        final boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // sanity check
        if (dw * dh == 0) {
            Log.e(MyApplication.TAG, "Invalid surface size");
            return;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mVideoSarDen == mVideoSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mVideoSarNum / mVideoSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_HORIZONTAL:
                dh = dw / ar;
                break;
            case SURFACE_FIT_VERTICAL:
                dw = dh * ar;
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        // set display size
        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        mSurfaceView.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = mSurfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        mSurfaceFrame.setLayoutParams(lp);

        mSurfaceView.invalidate();
    }

    /**
     * IVLCVout.Callback
     */
    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mVideoSarNum = sarNum;
        mVideoSarDen = sarDen;
        updateVideoSurfaces();
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {
        Log.d(MyApplication.TAG, "IVLCVout onSurfacesCreated");
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {
        Log.d(MyApplication.TAG, "IVLCVout onSurfacesDestroyed");
    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {
        Log.d(MyApplication.TAG, "IVLCVout temporary method, will be removed when VLC can handle decoder fallback");
    }

    /**
     * MediaPlayer.EventListener
     */
    @Override
    public void onEvent(MediaPlayer.Event event) {
        Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + event.type);
        switch (event.type) {
            case MediaPlayer.Event.MediaChanged:
                Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + "MediaChanged");    //256
                i = System.currentTimeMillis();
                break;
            case MediaPlayer.Event.Opening:
                Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + "Opening"); //258
                Snackbar.make(mSurfaceFrame, "start", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                progressBar.setVisibility(View.VISIBLE);
                break;
            case MediaPlayer.Event.Playing:
                Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + "Playing"); //260
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis() - i);
                Snackbar.make(mSurfaceFrame, "time=" + calendar.get(Calendar.SECOND) + " millsecond=" + calendar.get(Calendar.MILLISECOND), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                progressBar.setVisibility(View.GONE);
                break;
            case MediaPlayer.Event.Paused:
                Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + "Paused");      //261   --
                break;
            case MediaPlayer.Event.Stopped:
                Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + "Stopped"); //262       --
                break;
            case MediaPlayer.Event.EndReached:
                Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + "EndReached");  //265   --
                break;
            case MediaPlayer.Event.EncounteredError:
                Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + "EncounteredError");//266--
                break;
            case MediaPlayer.Event.TimeChanged:
                Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + "TimeChanged"); //267   +++
                break;
            case MediaPlayer.Event.PositionChanged:
                Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + "PositionChanged"); //268+++
                break;
            case MediaPlayer.Event.SeekableChanged:
                Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + "SeekableChanged"); //269   +
                break;
            case MediaPlayer.Event.PausableChanged:
                Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + "PausableChanged"); //270   +
                break;
            case MediaPlayer.Event.Vout:
                Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + "Vout");    //274   +
                break;
            case MediaPlayer.Event.ESAdded:
                Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + "ESAdded");     //276   ++
                break;
            case MediaPlayer.Event.ESDeleted:
                Log.d(MyApplication.TAG, "MediaPlayer.EventListener onEvent=" + "ESDeleted");   //277   ++
                break;

        }
    }

    /**
     * Player
     */

    private void createMediaPlayer() {  //создаем плеер
        Log.d(MyApplication.TAG, "createMediaPlayer"
                + "\nmVideoUrl=" + mVideoUrl
        );
        try {
            ivlcVout = mMediaPlayer.getVLCVout();
            ivlcVout.setVideoView(mSurfaceView);
            ivlcVout.attachViews();
            ivlcVout.addCallback(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (mOnLayoutChangeListener == null) {
                    mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
                        private final Runnable mRunnable = new Runnable() {
                            @Override
                            public void run() {
                                updateVideoSurfaces();
                            }
                        };

                        @Override
                        public void onLayoutChange(View v, int left, int top, int right,
                                                   int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                                mHandler.removeCallbacks(mRunnable);
                                mHandler.post(mRunnable);
                            }
                        }
                    };
                }
                mSurfaceFrame.addOnLayoutChangeListener(mOnLayoutChangeListener);
            }
        } catch (Exception e) {
            Log.d(MyApplication.TAG, "createMediaPlayer:" + e.toString());
        }
    }

    private void destroyPlayer() { //вызвать при onDestroy()
        Log.d(MyApplication.TAG, "destroyPlayer");
        try {
            mMediaPlayer.release();
            mLibVLC.release();
        } catch (Exception e) {
            Log.d(MyApplication.TAG, "destroyPlayer" + e.toString());
        }
    }


    private void stopPlayer() { //вызвать при onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (mOnLayoutChangeListener != null) {
                mSurfaceFrame.removeOnLayoutChangeListener(mOnLayoutChangeListener);
                mOnLayoutChangeListener = null;
            }
        }
        mMediaPlayer.stop();
        ivlcVout.detachViews();
        ivlcVout.removeCallback(this);
    }

    /**
     * Player control
     */
    private void playMediaPlayer() {
        Log.d(MyApplication.TAG, "playMediaPlayer mVideoUrl=" + mVideoUrl);
        try {
            Media media = new Media(mLibVLC, Uri.parse(mVideoUrl));
            mMediaPlayer.setMedia(media);
            media.release();
            mMediaPlayer.play();
        } catch (Exception e) {
            Log.d(MyApplication.TAG, "startMediaPlayer" + e.toString());
        }
    }


}
