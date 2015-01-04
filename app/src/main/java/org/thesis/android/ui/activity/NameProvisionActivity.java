package org.thesis.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.thesis.android.CApplication;
import org.thesis.android.R;
import org.thesis.android.io.database.SQLiteDAO;
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
        final View buttonMoveOnWrapper = findViewById(R.id.button_proceed_wrapper);

        nameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int
                    start, int count, int after) {
                //Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (buttonMoveOnWrapper.isShown() && TextUtils.isEmpty(s.toString())) {
                    TranslateAnimation animate = new TranslateAnimation(0, 0, 0,
                            buttonMoveOnWrapper.getHeight());
                    animate.setDuration(context.getResources().getInteger(R.integer
                            .floating_label_layout_duration_millis));
                    animate.setFillAfter(Boolean.TRUE);
                    buttonMoveOnWrapper.startAnimation(animate);
                    buttonMoveOnWrapper.setVisibility(View.INVISIBLE);
                } else if (!buttonMoveOnWrapper.isShown() && !TextUtils.isEmpty(s.toString())) {
                    TranslateAnimation animate = new TranslateAnimation(0, 0,
                            buttonMoveOnWrapper.getHeight(), 0);
                    animate.setDuration(context.getResources().getInteger(R.integer
                            .floating_label_layout_duration_millis));
                    animate.setFillAfter(Boolean.TRUE);
                    buttonMoveOnWrapper.startAnimation(animate);
                    buttonMoveOnWrapper.setVisibility(View.VISIBLE);
                }
            }
        });

        findViewById(R.id.button_proceed).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = nameField.getText().toString();
                PreferenceAssistant.saveSharedString(context,
                        PreferenceAssistant.PREF_USER_NAME, name);
                SQLiteDAO.getInstance().addUserName(name);
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
