package org.thesis.android.io.net;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.thesis.android.dev.CLog;
import org.thesis.android.util.Utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

public final class HTTPRequestsSingleton {

    private static final Object LOCK = new Object();
    public static final Integer SC_OK = 200;
    private static final Integer DEFAULT_HTTP_PORT = 80;
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

    public static String httpEncodeAndStringify(String hostWithPort, String path, String query) {
        final String host;
        final Integer port;
        final StringTokenizer tokenizer = new StringTokenizer(hostWithPort, ":");
        host = tokenizer.nextToken();
        if (tokenizer.countTokens() == 1) {
            port = Integer.parseInt(tokenizer.nextToken());
        } else {
            port = DEFAULT_HTTP_PORT;
        }

        final URI uri;

        try {
            uri = new URI("http", null, host, port, path, query, null);
        } catch (URISyntaxException e) {
            //Should never happen
            CLog.wtf(e);
            return null;
        }

        return uri.toASCIIString();
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
