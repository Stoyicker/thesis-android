package org.thesis.android.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;

import org.thesis.android.CApplication;

import java.util.List;

public final class Utils {

    private Utils() throws IllegalAccessException {
        throw new IllegalAccessException("Do not attempt to construct this class, ever.");
    }

    public static Boolean isInternetReachable() {
        final Context context = CApplication.getInstance().getContext();
        Boolean ret;

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo =
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI),
                dataNetworkInfo =
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        Boolean isWifiConnected =
                (wifiNetworkInfo == null) ? Boolean.FALSE : wifiNetworkInfo.isConnected(),
                isDataConnected =
                        (dataNetworkInfo == null) ? Boolean.FALSE :
                                dataNetworkInfo.isConnected();
        ret = isWifiConnected || isDataConnected;

        return ret;
    }

    public static Integer getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // Should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static Boolean isRunningOnForeground(Context context) {
        if (Utils.isMainThread())
            throw new IllegalStateException("Cannot call isRunningOnForeground on the main thread");

        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context
                .ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null) {
            return Boolean.FALSE;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo
                    .IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    private static Boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
