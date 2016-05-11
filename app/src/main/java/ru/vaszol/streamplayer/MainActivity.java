package ru.vaszol.streamplayer;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import ru.vaszol.streamplayer.util.LibVLCUtil;

public class MainActivity extends AppCompatActivity
        implements
        IVLCVout.Callback,
        MediaPlayer.EventListener {

    private String mVideoUrl;
    private String videoType;
//    private Uri mVideoUrl;

    // media player
    private LibVLC libvlc;
//    private VideoView videoView;

    private SurfaceView mSurfaceView;
    private FrameLayout surfaceFrame;
    private SurfaceHolder surfaceHolder;
    private IVLCVout vlcVout;
    private MediaPlayer mediaPlayer;

    private int videoWidth;
    private int videoHight;

    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mVideoVisibleWidth;
    private int mVideoVisibleHeight;
    private final static int VideoSizeChanged = -1;
    private ProgressBar progressBar;

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

//        mVideoUrl = Uri.parse(MyApplication.vidAddress1);
        mVideoUrl = MyApplication.vidAddress1;
        videoType = "Internet";

        mSurfaceView = (SurfaceView) findViewById(R.id.player_surface);
        surfaceHolder = mSurfaceView.getHolder();
        surfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);
//        createMediaPlayer();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        setSize(mVideoWidth, mVideoHeight);
        setSize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        createMediaPlayer();
//        resumeMediaPlayer();
//        playMediaPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    /**
     * Listebers
     */
    public void onClick(View view) {
        Log.d(MyApplication.TAG, "onClick=" + view.toString());
//        if (view.getId() == R.id.fab) {
//            if (mVideoUrl.equals(MyApplication.vidAddress2)) {
////                mVideoUrl = Uri.parse(MyApplication.vidAddress1);
//                mVideoUrl = MyApplication.vidAddress1;
//                videoType = "Internet";
//            } else if (mVideoUrl.equals(MyApplication.vidAddress1)) {
////                mVideoUrl = Uri.parse(MyApplication.vidAddress2);
//                mVideoUrl = MyApplication.vidAddress2;
//                videoType = "Local";
//            }
//        }
//        if (view.getId() == R.id.video) {
//
//        }
        Log.d(MyApplication.TAG, "path=" + mVideoUrl);
//        createMediaPlayer();
        playMediaPlayer();
    }


    //    private void setSize(int width, int height) {
    private void setSize() {
//        mVideoWidth = width;
//        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

//        if (mVideoWidth == null || videoView == null)
//            return;

// get screen size
        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();

// getWindow().getDecorView() doesn't always take orientation into
// account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

// force surface buffer size
        mSurfaceView.getHolder().setFixedSize(mVideoWidth, mVideoHeight);

// set display size
//        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
//        lp.width = w;
//        lp.height = h;
//        mSurfaceView.setLayoutParams(lp);
//        mSurfaceView.invalidate();



//        mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
        Log.d(MyApplication.TAG, "changeSurfaceSize mSurfaceHolder.setFixedSize " + mVideoWidth + " " + mVideoHeight);
        Log.d(MyApplication.TAG, "changeSurfaceSize mSurface layout " + lp.width + " " + lp.height);
        lp.width = (int) Math.ceil(w * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(h * mVideoHeight / mVideoVisibleHeight);
        mSurfaceView.setLayoutParams(lp);
        Log.d(MyApplication.TAG, "changeSurfaceSize mSurface layout " + lp.width + " " + lp.height);
        // set frame size (crop if necessary)
        lp = surfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(w);
        lp.height = (int) Math.floor(h);
        surfaceFrame.setLayoutParams(lp);

        mSurfaceView.invalidate();  //перепрорисовка
        Log.d(MyApplication.TAG, "changeSurfaceSize surfaceFrame layout " + lp.width + " " + lp.height);
        Log.d(MyApplication.TAG, "changeSurfaceSize mSurface " + mSurfaceView.getWidth() + " " + mSurfaceView.getHeight());
    }

//    private void createMediaPlayer() {
//        releasePlayer();
//
//        try {
//            if (mVideoUrl.length() > 0) {
//                Toast toast = Toast.makeText(this, mVideoUrl, Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
//                        0);
//                toast.show();
//            }
//
//            // Create LibVLC
//            ArrayList<String> options = new ArrayList<String>();
//            //options.add("--subsdec-encoding <encoding>");
//            options.add("--aout=opensles");
//            options.add("--audio-time-stretch"); // time stretching
//            options.add("-vvv"); // verbosity
//            libvlc = new LibVLC(options);
//            libvlc.setOnHardwareAccelerationError(this);
//
//
//            mMediaPlayer = new MediaPlayer(libvlc);
//            mMediaPlayer.setEventListener(mPlayerListener);
//
//            videoView.setVideoPath(mVideoUrl);
//            videoView.setVideoURI(Uri.parse(mVideoUrl));
//            videoView.setMediaController(new MediaController(this));
//            videoView.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(android.media.MediaPlayer mp) {
//                    Log.d(MyApplication.TAG, "OnPrepared called");
//                }
//            });
//
//            videoView.start();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//
//            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    // TODO: handle this cleaner
//    private void releasePlayer() {
//        if (libvlc == null)
//            return;
//        mMediaPlayer.stop();
//        final IVLCVout vout = mMediaPlayer.getVLCVout();
//        vout.removeCallback(this);
//        vout.detachViews();
//        videoView = null;
//        libvlc.release();
//        libvlc = null;
//
//        mVideoWidth = 0;
//        mVideoHeight = 0;
//    }
//
//    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    /**
     * IVLCVout.Callback
     */
    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        Log.d(MyApplication.TAG, "onNewLayout");
        try {
//            totalTime = mediaPlayer.getLength();
//            seekBarTime.setMax((int) totalTime);
//            tvTotalTime.setText(SystemUtil.getMediaTime((int) totalTime));

            videoWidth = width;
            videoHight = height;
            mVideoWidth = width;
            mVideoHeight = height;
            mVideoVisibleWidth=visibleWidth;
            mVideoVisibleHeight=visibleHeight;
            setSize();
//            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//            Display display = windowManager.getDefaultDisplay();
//            Point point = new Point();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//                display.getSize(point);
//            }

//            ViewGroup.LayoutParams layoutParams = mSurfaceView.getLayoutParams();
//            layoutParams.width = point.x;
//            layoutParams.height = (int) Math.ceil((float) videoHight * (float) point.x / (float) videoWidth);
//            mSurfaceView.setLayoutParams(layoutParams);


        } catch (Exception e) {
            Log.d(MyApplication.TAG, e.toString());
        }
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {
        Log.d(MyApplication.TAG, "onSurfacesCreated");
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {
        Log.d(MyApplication.TAG, "onSurfacesDestroyed");
    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {
        Log.d(MyApplication.TAG, "temporary method, will be removed when VLC can handle decoder fallback");
    }

    /**
     * MediaPlayer.EventListener
     */
    @Override
    public void onEvent(MediaPlayer.Event event) {
//        Log.d(MyApplication.TAG, "onEvent=" + event);
        try {
//            if (event.getTimeChanged() == 0 || totalTime == 0 || event.getTimeChanged() > totalTime) {
//                return;
//            }

//            seekBarTime.setProgress((int) event.getTimeChanged());
//            tvCurrentTime.setText(SystemUtil.getMediaTime((int) event.getTimeChanged()));

            //播放结束  люфт
            if (mediaPlayer.getPlayerState() == Media.State.Ended) {
//                seekBarTime.setProgress(0);
                mediaPlayer.setTime(0);
//                tvTotalTime.setText(SystemUtil.getMediaTime((int) totalTime));
                mediaPlayer.stop();
//                imgPlay.setBackgroundResource(R.drawable.videoviewx_play);
                Log.d(MyApplication.TAG, "onEvent getPlayerState=" + mMediaPlayer.getPlayerState());
            }
        } catch (Exception e) {
            Log.d(MyApplication.TAG, e.toString());
        }
    }

//
//    private static class MyPlayerListener implements MediaPlayer.EventListener {
//        private WeakReference mOwner;
//
//        public MyPlayerListener(MainActivity owner) {
//            mOwner = new WeakReference<MainActivity>(owner);
//        }
//
//        @Override
//        public void onEvent(MediaPlayer.Event event) {
//            MainActivity player = (MainActivity) mOwner.get();
//
//            switch (event.type) {
//                case MediaPlayer.Event.EndReached:
//                    Log.d(MyApplication.TAG, "MediaPlayerEndReached");
//                    player.releasePlayer();
//                    break;
//                case MediaPlayer.Event.Playing:
//                case MediaPlayer.Event.Paused:
//                case MediaPlayer.Event.Stopped:
//                default:
//                    break;
//            }
//        }
//    }
//
//    @Override
//    public void eventHardwareAccelerationError() {
//        Log.e(MyApplication.TAG, "Error with hardware acceleration");
//        this.releasePlayer();
//        Toast.makeText(this, "Error with hardware acceleration", Toast.LENGTH_LONG).show();
//
//    }

    /**
     * Player
     */

    private void createMediaPlayer() {  //создаем плеер
        Log.d(MyApplication.TAG, "createMediaPlayer"
                + "\nmVideoUrl=" + mVideoUrl
                + "\nvideoType=" + videoType
        );
        try {
            LibVLC libvlc = LibVLCUtil.getLibVLC(null);
            mediaPlayer = new MediaPlayer(libvlc);
            vlcVout = mediaPlayer.getVLCVout();
            vlcVout.addCallback(this);
            vlcVout.setVideoView(mSurfaceView);
            vlcVout.attachViews();

//            Media media = new Media(libvlc, mVideoUrl);
            Media media;
            if (videoType.equals("Local")) {
                media = new Media(libvlc, mVideoUrl);
            } else {
                media = new Media(libvlc, Uri.parse(mVideoUrl));
            }


            mediaPlayer.setMedia(media);
            mediaPlayer.setEventListener(this);
            mediaPlayer.play();
        } catch (Exception e) {
            Log.d(MyApplication.TAG, "createMediaPlayer:" + e.toString());
        }
    }

    private void resumeMediaPlayer() {  //вызвать при onResume()
//        vlcVout.setVideoView(mSurfaceView);
//        vlcVout.attachViews();
//        vlcVout.addCallback(this);
//        mediaPlayer.setEventListener(this);
    }

    private void releasePlayer() { //вызвать при onDestroy()
        Log.d(MyApplication.TAG, "releasePlayer");
        try {
//            super.onDestroy();
//            pausePlay();
            pauseMediaPlayer();
            mediaPlayer.release();
        } catch (Exception e) {
            Log.d(MyApplication.TAG, "releasePlayer" + e.toString());
        }
    }

    /**
     * Player control
     */
    private void playMediaPlayer() {
        Log.d(MyApplication.TAG, "playMediaPlayer mVideoUrl=" + mVideoUrl);
        try {
            pauseMediaPlayer();
//            Media media = new Media(libvlc, mVideoUrl);
            Media media;
            if (videoType.equals("Local")) {
                media = new Media(libvlc, mVideoUrl);
            } else {
                media = new Media(libvlc, Uri.parse(mVideoUrl));
            }
            mediaPlayer.setMedia(media);
            mediaPlayer.play();
        } catch (Exception e) {
            Log.d(MyApplication.TAG, "startMediaPlayer" + e.toString());
        }
    }

    private void pauseMediaPlayer() {
        Log.d(MyApplication.TAG, "pauseMediaPlayer");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }

        vlcVout.detachViews();
        vlcVout.removeCallback(this);
        mediaPlayer.setEventListener(null);
    }

}
