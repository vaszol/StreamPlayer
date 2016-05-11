package ru.vaszol.streamplayer.util;

import org.videolan.libvlc.LibVLC;

import java.util.ArrayList;

public class LibVLCUtil {
    private static LibVLC libVLC = null;

    public synchronized static LibVLC getLibVLC(ArrayList<String> options) throws IllegalStateException {
        if (libVLC == null) {
            if (options == null) {
                libVLC = new LibVLC();
            } else {
                libVLC = new LibVLC(options);
            }
        }
        return libVLC;
    }
}