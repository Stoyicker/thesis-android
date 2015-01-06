package org.thesis.android.ui.card.tag;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.thesis.android.R;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.ui.activity.NavigationDrawerActivity;
import org.thesis.android.ui.util.FlowLayout;

import java.util.LinkedList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.CardExpand;

public class TagCloudCardExpand extends CardExpand implements ITagCard.ITagChangedListener,
        NavigationDrawerActivity.IOnBackPressedListener {

    private final List<ITagCard> mTagCardViews = new LinkedList<>();
    private final ITagCard.ITagChangedListener mCallback;
    private FlowLayout mFlowLayout;

    public TagCloudCardExpand(Context context, ITagCard.ITagChangedListener _callback,
                              String groupName,
                              View expandView) {
        super(context, R.layout.card_tag_group_flow);

        mCallback = _callback;

        List<String> tags = SQLiteDAO.getInstance().getGroupTags(groupName);

        for (String x : tags) {
            mTagCardViews.add(new TagCardView(mContext, x, this));
        }

        expandView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagCloudCardExpand.this.processClick();
            }
        });
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
            mTagCardViews.add(new AddedTagCardView(mContext, TagCloudCardExpand.this));
        else
            viewToRemove.cancelCreation();
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        if (view == null) return;

        mFlowLayout = (FlowLayout) view.findViewById(R.id.flow_layout);

        mFlowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagCloudCardExpand.this.processClick();
            }
        });

        for (ITagCard v : mTagCardViews) {
            mFlowLayout.addView((View) v);
        }
    }

    private void recalculateFlowLayoutHeight() {
        mFlowLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
    }

    @Override
    public void onTagCreated(ITagCard tag) {
        mTagCardViews.add(tag);
        mFlowLayout.addView((View) tag);
        recalculateFlowLayoutHeight();
    }

    @Override
    public void onTagAdded(ITagCard tag) {
        if (mCallback != null)
            mCallback.onTagAdded(tag);
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
            viewToRemove.cancelCreation(); //Discard the added tag

        return Boolean.TRUE;
    }
}
