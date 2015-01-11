package org.thesis.android;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

public class CApplication extends Application {
    private static CApplication mInstance;
    private static Context mContext;

    public static CApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public synchronized Context getContext() {
        if (mContext == null) {
            return getApplicationContext();
        }
        return mContext;
    }

    public void setContext(@NonNull Context context) {
        mContext = context;
    }
}