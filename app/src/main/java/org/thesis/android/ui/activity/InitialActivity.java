package org.thesis.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import org.thesis.android.CApplication;
import org.thesis.android.dev.CLog;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.io.file.FileOperations;
import org.thesis.android.io.prefs.PreferenceAssistant;

import java.io.File;

public class InitialActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = initContext();
        flushCacheIfNecessary(context);
        initDatabase(context);

        launchHomeActivity(context);
    }

    private void initDatabase(Context context) {
        SQLiteDAO.setup(context);
    }

    private void launchHomeActivity(Context context) {
        Class c;
        if (PreferenceAssistant.readSharedBoolean(context,
                PreferenceAssistant.PREF_USER_HAS_SET_NAME, Boolean.FALSE)) {
            c = NavigationDrawerActivity.class;
        } else c = NameProvisionActivity.class;
        final Intent homeIntent = new Intent(context, c);
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

    private Context initContext() {
        Context ret;
        CApplication.getInstance().setContext(ret = getSupportActionBar().getThemedContext());

        return ret;
    }
}