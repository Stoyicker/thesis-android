package org.thesis.android.ui.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.thesis.android.R;
import org.thesis.android.io.database.SQLiteDAO;

import java.util.LinkedList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.CardExpand;

public class TagCloudCardExpand extends CardExpand implements TagCardView.ITagRemovalListener {

    private final List<TagCardView> mTagCardViews = new LinkedList<>();
    private final ITagRemovalListener mCallback;
    private FlowLayout mFlowLayout;

    public TagCloudCardExpand(Context context, ITagRemovalListener _callback, String groupName) {
        super(context, R.layout.card_tag_group_flow);

        mCallback = _callback;

        List<String> tags = SQLiteDAO.getInstance().getGroupTags(groupName);

        for (String x : tags) {
            mTagCardViews.add(new TagCardView(mContext, x, this));
        }

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        if (view == null) return;

        mFlowLayout = (FlowLayout) view.findViewById(R.id.flow_layout);

        mFlowLayout.setBackgroundColor(mContext.getResources().getColor(android.R.color
                .holo_red_dark));

        mFlowLayout.removeAllViews();
        for (View v : mTagCardViews) {
            mFlowLayout.addView(v);
        }
    }

    private void recalculateHeight() {
        mFlowLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        //FIXME Use mFlowLayout.getCalculatedHeight() to update the height of the expand
    }

    @Override
    public void onTagRemoved(TagCardView removedTagView) {
        mTagCardViews.remove(removedTagView);
        if (mCallback != null)
            mCallback.onTagRemoved(removedTagView);
        recalculateHeight();
    }

    public interface ITagRemovalListener {
        public void onTagRemoved(TagCardView removedTagView);
    }
}
