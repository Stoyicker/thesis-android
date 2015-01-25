package org.thesis.android.io.net;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.thesis.android.dev.CLog;
import org.thesis.android.util.Utils;

import java.io.IOException;

public final class HTTPRequestsSingleton {

    private static final Object LOCK = new Object();
    public static final Integer SC_OK = 200;
    private static volatile HTTPRequestsSingleton mInstance;
    private final OkHttpClient mClient;

    private HTTPRequestsSingleton() {
        mClient = new OkHttpClient();
    }

    public static HTTPRequestsSingleton getInstance() {
        HTTPRequestsSingleton ret = mInstance;
        if (ret == null) {
            synchronized (LOCK) {
                ret = mInstance;
                if (ret == null) {
                    ret = new HTTPRequestsSingleton();
                    mInstance = ret;
                }
            }
        }
        return ret;
    }

    @Nullable
    public Response performRequest(@NonNull Request request) {
        if (!Utils.isInternetReachable())
            return null;
        try {
            return mClient.newCall(request).execute();
        } catch (IOException e) {
            CLog.wtf(e);
            return null;
        }
    }
}
