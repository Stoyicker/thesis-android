package org.thesis.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.thesis.android.CApplication;
import org.thesis.android.R;
import org.thesis.android.io.prefs.PreferenceAssistant;

public class NameProvisionActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar supportActionBar;
        if ((supportActionBar = getSupportActionBar()) != null)
            supportActionBar.hide();

        setContentView(R.layout.activity_name_provision);

        final Context context = CApplication.getInstance().getContext();
        final EditText nameField = (EditText) findViewById(R.id.name_field);

        (findViewById(R.id.button_proceed)).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceAssistant.saveSharedString(context,
                        PreferenceAssistant.PREF_USER_NAME, nameField.getText().toString());
                PreferenceAssistant.saveSharedBoolean(context,
                        PreferenceAssistant.PREF_USER_HAS_SET_NAME, Boolean.TRUE);
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final InputMethodManager imm = (InputMethodManager) context
                                .getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(nameField.getWindowToken(), 0);
                        final Intent homeIntent = new Intent(context,
                                NavigationDrawerActivity.class);
                        finish();
                        startActivity(homeIntent);
                    }
                }, 400);
            }
        });
    }


}
