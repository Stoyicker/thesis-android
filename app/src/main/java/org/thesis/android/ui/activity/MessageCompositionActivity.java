package org.thesis.android.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import org.thesis.android.CApplication;
import org.thesis.android.R;
import org.thesis.android.ui.card.tag.AddedTagCardView;
import org.thesis.android.ui.component.FlowLayout;

import java.util.LinkedList;
import java.util.List;

public class MessageCompositionActivity extends ActionBarActivity {

    public static final String EXTRA_TAG = "EXTRA_TAG";
    private FlowLayout mFlowLayout;
    private Context mContext;
    private final List<AddedTagCardView> tags = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_composition);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setTitle(R.string.action_compose);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);

        mContext = CApplication.getInstance().getContext();

        mFlowLayout = (FlowLayout) findViewById(R.id.tag_container);
        mFlowLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                final AddedTagCardView tag;
//                mFlowLayout.addView(tag = new AddedTagCardView(mContext,
//                        null, findViewById(android.R.id.content)));
//                tags.add(tag);
                return Boolean.FALSE;
            }
        });
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
