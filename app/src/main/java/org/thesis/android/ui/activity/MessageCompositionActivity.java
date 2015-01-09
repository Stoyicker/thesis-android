package org.thesis.android.ui.activity;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.thesis.android.R;

public class MessageCompositionActivity extends ActionBarActivity {

    public static final String EXTRA_TAG = "EXTRA_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_composition);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setTitle(R.string.action_compose);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);
    }

    private void requestActivityReturn() {
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(R.anim.move_in_from_bottom, R.anim.move_out_to_bottom);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                requestActivityReturn();
                return Boolean.TRUE;
        }
        return super.onOptionsItemSelected(item);
    }
}
