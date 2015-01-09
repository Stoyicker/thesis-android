package org.thesis.android.ui.card.tag;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import org.apache.commons.lang3.text.WordUtils;
import org.thesis.android.R;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.ui.activity.NavigationDrawerActivity;
import org.thesis.android.ui.component.FlowLayout;

import java.util.LinkedList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.CardExpand;

public class TagCloudCardExpand extends CardExpand implements ITagCard.ITagChangedListener,
        NavigationDrawerActivity.IOnBackPressedListener {

    private final List<ITagCard> mTagCardViews = new LinkedList<>();
    private final ITagCard.ITagChangedListener mCallback;
    private FlowLayout mFlowLayout;
    private ScrollView mScrollView;
    private final View mDummy;

    public TagCloudCardExpand(Context context, ITagCard.ITagChangedListener _callback,
                              View dummy) {
        super(context, R.layout.card_tag_group_flow);

        mCallback = _callback;

        mDummy = dummy;

        mDummy.setFocusable(Boolean.TRUE);
        mDummy.setFocusableInTouchMode(Boolean.TRUE);

        dummy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagCloudCardExpand.this.processClick();
            }
        });
    }

    public void setTagGroupAndRefreshViews(String groupName) {
        final List<String> tags = SQLiteDAO.getInstance().getGroupTags(groupName);

        mTagCardViews.clear();

        for (String x : tags) {
            mTagCardViews.add(new TagCardView(mContext, x, this));
        }
    }

    private synchronized void processClick() {
        AddedTagCardView viewToRemove = null;
        for (ITagCard c : mTagCardViews) {
            if (!(c instanceof AddedTagCardView)) continue;
            final AddedTagCardView castedC = (AddedTagCardView) c;
            if (castedC.isBeingBuilt()) {
                viewToRemove = castedC;
                break;
            }
        }
        if (viewToRemove == null)
            mTagCardViews.add(new AddedTagCardView(mContext, TagCloudCardExpand.this, mDummy));
        else
            viewToRemove.cancelTagCreation();
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        if (view == null) return;

        mScrollView = (ScrollView) parent;

        mFlowLayout = (FlowLayout) view.findViewById(R.id.flow_layout);

        mFlowLayout.removeAllViews();

        mScrollView.setOnTouchListener(new View.OnTouchListener() {

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
                    case MotionEvent.ACTION_UP: {
                        float endX = event.getX();
                        float endY = event.getY();
                        if (isClick(startX, endX, startY, endY)) {
                            TagCloudCardExpand.this.processClick();
                            return Boolean.TRUE;
                        }
                        break;
                    }
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

        for (ITagCard v : mTagCardViews) {
            mFlowLayout.addView((View) v);
        }
    }

    private void recalculateFlowLayoutHeight() {
        if (mFlowLayout != null)
            mFlowLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
    }

    @Override
    public void onTagCreated(ITagCard tag) {
        //No need to add the tag here
        mFlowLayout.addView((View) tag);
        mScrollView.smoothScrollTo(0, mScrollView.getHeight());
        recalculateFlowLayoutHeight();
    }

    @Override
    public void onTagAdded(ITagCard tag) {
        mTagCardViews.remove(tag);
        mFlowLayout.removeView((View) tag);
        final TagCardView v = new TagCardView(mContext, WordUtils.capitalizeFully(tag.getName()),
                this);
        mTagCardViews.add(v);
        if (mCallback != null)
            mCallback.onTagAdded(tag);
        mFlowLayout.addView(v);
        recalculateFlowLayoutHeight();
    }

    @Override
    public void onTagRemoved(ITagCard tag) {
        mTagCardViews.remove(tag);
        if (mCallback != null)
            mCallback.onTagRemoved(tag);
        mFlowLayout.removeView((View) tag);
        recalculateFlowLayoutHeight();
    }

    @Override
    public void onTagCreationCancelled(ITagCard tag) {
        mTagCardViews.remove(tag);
        mFlowLayout.removeView((View) tag);
        recalculateFlowLayoutHeight();
    }

    @Override
    public Boolean onBackPressed() {
        if (!getParentCard().isExpanded()) return Boolean.FALSE;
        AddedTagCardView viewToRemove = null;
        for (ITagCard c : mTagCardViews) {
            if (!(c instanceof AddedTagCardView)) continue;
            final AddedTagCardView castedC = (AddedTagCardView) c;
            if (castedC.isBeingBuilt()) {
                viewToRemove = castedC;
                break;
            }
        }
        if (viewToRemove == null)
            getParentCard().doToogleExpand(); //Close Expand
        else
            viewToRemove.cancelTagCreation(); //Discard the added tag

        return Boolean.TRUE;
    }

    public void cancelEdits() {
        if (!getParentCard().isExpanded()) return;
        AddedTagCardView viewToRemove = null;
        for (ITagCard c : mTagCardViews) {
            if (!(c instanceof AddedTagCardView)) continue;
            final AddedTagCardView castedC = (AddedTagCardView) c;
            if (castedC.isBeingBuilt()) {
                viewToRemove = castedC;
                break;
            }
        }
        if (viewToRemove != null)
            viewToRemove.cancelTagCreation(); //Discard the added tag
    }
}
