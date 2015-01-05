package org.thesis.android.ui.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.thesis.android.R;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import it.gmariotti.cardslib.library.internal.CardExpand;

public class TagCloudCardExpand extends CardExpand implements TagCardView.ITagRemovalListener {

    private final List<TagCardView> mTagCardViews = new LinkedList<>();
    private final ITagRemovalListener mCallback;
    private FlowLayout mFlowLayout;
    private View mView;

    public TagCloudCardExpand(Context context, ITagRemovalListener _callback) {
        super(context, R.layout.card_tag_group_flow);

        mCallback = _callback;

        //TODO Retrieve the real, UNIQUE and FORMATTED tags
        List<String> tags = new LinkedList<>();
        for (int i = 0; i < 10; i++)
            tags.add("tag " + new Random().nextFloat());

        for (String x : tags) {
            mTagCardViews.add(new TagCardView(mContext, x, this));
        }

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        if (view == null) return;

        mView = view;

        mFlowLayout = (FlowLayout) mView.findViewById(R.id.flow_layout);

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
