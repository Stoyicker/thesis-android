package org.thesis.android.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apache.commons.lang3.text.WordUtils;
import org.thesis.android.CApplication;
import org.thesis.android.R;
import org.thesis.android.ui.card.tag.AddedTagCardView;
import org.thesis.android.ui.card.tag.ITagCard;
import org.thesis.android.ui.card.tag.TagCardView;
import org.thesis.android.ui.component.FlowLayout;

import java.util.LinkedList;
import java.util.List;

public class MessageCompositionActivity extends ActionBarActivity implements ITagCard
        .ITagChangedListener {

    public static final String EXTRA_TAG = "EXTRA_TAG";
    private final List<ITagCard> mTags = new LinkedList<>();
    private FlowLayout mFlowLayout;
    private Context mContext;
    private SlidingUpPanelLayout mSlidingPaneLayout;

    //TODO Does it actually scroll? If it doesn't, consider the recalculateFlowLayoutHeight-way
    // in TagCloudCardExpand

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_composition);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setTitle(R.string.action_compose);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);

        mContext = CApplication.getInstance().getContext();

        toolbar.findViewById(R.id.action_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        tryToSendCurrentMessage();
                    }
                });
            }
        });

        mSlidingPaneLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mFlowLayout = (FlowLayout) mSlidingPaneLayout.findViewById(R.id.tag_container);

        mFlowLayout.setOnTouchListener(new View.OnTouchListener() {

            public static final float CLICK_ACTION_THRESHOLD = 5;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        if (isClick(startX, endX, startY, endY) && mSlidingPaneLayout
                                .isPanelExpanded()) {
                            for (ITagCard c : mTags) {
                                if (!(c instanceof AddedTagCardView)) continue;
                                final AddedTagCardView castedC = (AddedTagCardView) c;
                                if (castedC.isBeingBuilt()) {
                                    ((AddedTagCardView) c).cancelTagCreation();
                                    return Boolean.TRUE;
                                }
                            }
                            final AddedTagCardView tag;
                            mFlowLayout.addView(tag = new AddedTagCardView(mContext,
                                    MessageCompositionActivity.this,
                                    findViewById(android.R.id.content)));
                            mTags.add(tag);
                            return Boolean.TRUE;
                        }
                        break;
                }
                return Boolean.FALSE;
            }

            private boolean isClick(float startX, float endX, float startY, float endY) {
                float differenceX = Math.abs(startX - endX);
                float differenceY = Math.abs(startY - endY);
                return !(differenceX > CLICK_ACTION_THRESHOLD || differenceY >
                        CLICK_ACTION_THRESHOLD);
            }
        });
    }

    @Override
    public void onTagCreated(ITagCard tag) {
        //Do nothing, it as already added to both
    }

    @Override
    public void onTagAdded(ITagCard tag) {
        mTags.remove(tag);
        mFlowLayout.removeView((View) tag);
        final TagCardView v = new TagCardView(mContext, WordUtils.capitalizeFully(tag.getName()),
                this);
        mTags.add(v);
        mFlowLayout.addView(v);
    }

    @Override
    public void onTagRemoved(ITagCard tag) {
        mTags.remove(tag);
        mFlowLayout.removeView((View) tag);
    }

    @Override
    public void onTagCreationCancelled(ITagCard tag) {
        mTags.remove(tag);
        mFlowLayout.removeView((View) tag);
    }

    private void tryToSendCurrentMessage() {
        //TODO tryToSendCurrentMessage
        //Parse the list of children of the flowlayout, extracting the tags with instanceof
        //If there are no tags, toast and return
        //Parse the message body
        //If the message body is empty and there are no attachments, return
        //If the message body contains "attach" but there are no attachments show confirmatory
        // dialog
        //If here, send. To send, just use an asynctask (will it hold if the app is minimized?
        // and closed?)
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

    @Override
    public void onBackPressed() {
        Boolean consumed = Boolean.FALSE;

        AddedTagCardView viewToRemove = null;
        for (ITagCard c : mTags) {
            if (!(c instanceof AddedTagCardView)) continue;
            final AddedTagCardView castedC = (AddedTagCardView) c;
            if (castedC.isBeingBuilt()) {
                viewToRemove = castedC;
                break;
            }
        }
        if (viewToRemove == null) {
            if (mSlidingPaneLayout.isPanelExpanded()) {
                mSlidingPaneLayout.collapsePanel();
                consumed = Boolean.TRUE;
            }
        } else {
            viewToRemove.cancelTagCreation(); //Discard the added tag
            consumed = Boolean.TRUE;
        }

        if (!consumed)
            super.onBackPressed();
    }
}
