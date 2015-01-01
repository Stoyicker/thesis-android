package org.thesis.android.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import org.thesis.android.dev.CLog;

public class NavDrawerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CLog.e("testtag", "testmessage");
    }
}
