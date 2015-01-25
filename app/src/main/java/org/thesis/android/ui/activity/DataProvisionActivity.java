package org.thesis.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.thesis.android.CApplication;
import org.thesis.android.R;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.io.prefs.PreferenceAssistant;

public class DataProvisionActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar supportActionBar;
        if ((supportActionBar = getSupportActionBar()) != null)
            supportActionBar.hide();

        setContentView(R.layout.activity_name_provision);

        final Context context = CApplication.getInstance().getContext();
        final EditText nameField = (EditText) findViewById(R.id.name_field);
        nameField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event
                        .isShiftPressed() && (event
                        .getAction() == KeyEvent
                        .ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
                    if (TextUtils.isEmpty(v.getText()))
                        return Boolean.FALSE;
                    final String name = nameField.getText().toString();
                    PreferenceAssistant.saveSharedString(context,
                            PreferenceAssistant.PREF_USER_NAME, name);
                    SQLiteDAO.getInstance().addUserName(name);
                    PreferenceAssistant.saveSharedBoolean(context,
                            PreferenceAssistant.PREF_USER_HAS_SET_NAME, Boolean.TRUE);
                    v.post(new Runnable() {
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
                            overridePendingTransition(R.anim.move_in_from_bottom,
                                    R.anim.move_out_to_bottom);
                        }
                    });
                    return Boolean.TRUE;
                }

                return Boolean.FALSE;
            }
        });
    }


}
