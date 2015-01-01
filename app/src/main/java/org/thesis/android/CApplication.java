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

    public Context getContext() {
        if (mContext == null)
            throw new IllegalStateException("Context requested when it is null.");
        return mContext;
    }

    public void setContext(@NonNull Context context) {
        mContext = context;
    }
}