package org.thesis.android.devutil;

import android.util.Log;

import org.thesis.android.BuildConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CLog {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("HH:mm:ss", Locale
            .ENGLISH);

    public static void e(String msg) {
        Log.e(new Exception().getStackTrace()[1].getClassName(), FORMATTER.format(new Date()) + ": " + msg);
    }

    public static void d(String msg) {
        if (BuildConfig.DEBUG)
            Log.d(new Exception().getStackTrace()[1].getClassName(), FORMATTER.format(new Date()) + ": " + msg);
    }

    public static void wtf(Throwable e) {
        if (BuildConfig.DEBUG)
            Log.wtf(new Exception().getStackTrace()[1].getClassName(),
                    FORMATTER.format(new Date()) + ": " + e);
    }

    public static void i(String msg) {
        if (BuildConfig.DEBUG)
            Log.i(new Exception().getStackTrace()[1].getClassName(), FORMATTER.format(new Date()) + ": " + msg);
    }

    public static void w(String msg) {
        if (BuildConfig.DEBUG)
            Log.w(new Exception().getStackTrace()[1].getClassName(), FORMATTER.format(new Date())
                    + ": " + msg);
    }
}
