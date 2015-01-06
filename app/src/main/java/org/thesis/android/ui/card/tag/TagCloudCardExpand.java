package org.thesis.android.ui.card.tag;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.thesis.android.R;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.ui.util.FlowLayout;

import java.util.LinkedList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.CardExpand;

public class TagCloudCardExpand extends CardExpand implements TagCardView.ITagRemovalListener {

    private final List<ITagCard> mTagCardViews = new LinkedList<>();
    private final ITagRemovalListener mCallback;
    private FlowLayout mFlowLayout;

    public TagCloudCardExpand(Context context, ITagRemovalListener _callback, String groupName,
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
                mFlowLayout.addView(new AddedTagCardView(mContext, TagCloudCardExpand.this));
            }
        });

        //TODO Point both listeners to the same method, which will be synchronized and will only
        // work if there is no tag being created
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        if (view == null) return;

        mFlowLayout = (FlowLayout) view.findViewById(R.id.flow_layout);

        mFlowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlowLayout.addView(new AddedTagCardView(mContext, TagCloudCardExpand.this));
            }
        });

        mFlowLayout.removeAllViews();
        for (ITagCard v : mTagCardViews) {
            mFlowLayout.addView((View) v);
        }
    }

    private void recalculateFlowLayoutHeight() {
        mFlowLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
    }

    @Override
    public void onTagRemoved(ITagCard removedTagView) {
        mTagCardViews.remove(removedTagView);
        if (mCallback != null)
            mCallback.onTagRemoved(removedTagView);
        recalculateFlowLayoutHeight();
    }

    public interface ITagRemovalListener {
        public void onTagRemoved(ITagCard removedTagView);
    }
}
