package org.thesis.android.dev;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CLog {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("HH:mm:ss");

    public static void e(String tag, String msg) {
        Log.e(tag, FORMATTER.format(new Date()) + ": " + msg);
    }
}
