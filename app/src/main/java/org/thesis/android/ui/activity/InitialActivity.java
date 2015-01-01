package org.thesis.android.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.thesis.android.dev.CLog;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.io.file.FileOperations;

import java.io.File;

public class InitialActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getApplicationContext();
        flushCacheIfNecessary(context);
        initDatabase(context);

        launchHomeActivity(context);
    }

    private void initDatabase(Context context) {
        SQLiteDAO.setup(context);
    }

    private void launchHomeActivity(Context context) {
        final Intent homeIntent = new Intent(context, NavDrawerActivity.class);
        finish();
        startActivity(homeIntent);
    }

    private void flushCacheIfNecessary(Context context) {
        File cacheDir;
        final Integer CACHE_SIZE_LIMIT_BYTES = 1048576; //1MB, fair enough for the cache
        if ((cacheDir = context.getCacheDir()).length() >
                CACHE_SIZE_LIMIT_BYTES) {
            if (!FileOperations.recursivelyDelete(cacheDir)) {
                CLog.e(getClass().getName(), "Could not clean the cache.");
            }
        }
    }
}