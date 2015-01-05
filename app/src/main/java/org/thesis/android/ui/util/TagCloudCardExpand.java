package org.thesis.android.ui.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.thesis.android.R;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.CardExpand;

public class TagCloudCardExpand extends CardExpand {

    private final List<TagCardView> mTagCardViews = new LinkedList<>();

    public TagCloudCardExpand(Context context) {
        super(context, R.layout.card_tag_group_flow);

        //TODO Retrieve the real, UNIQUE and FORMATTED tags

        List<String> tags = Arrays.asList("tag 2", "tag 2", "tag 2", "tag 2", "tag 2", "tag 2",
                "TAGJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJTAGJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJTAGJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJTAGJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJTAGJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJTAGJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ",
                "tag 2", "tag 2", "tag 2", "tag 2", "tag 2", "tag 2",
                "tag 2", "tag 2", "tag 2", "tag 2", "tag 2", "tag 2",
                "tag 2", "tag 2", "tag 2", "tag 2", "tag 2", "FINAL");

        for (String x : tags) {
            mTagCardViews.add(new TagCardView(mContext, x));
        }
    }

    public void setupInnerViewElements(ViewGroup parent, View view) {

        if (view == null) return;

        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.flow_layout);

        viewGroup.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));

        for (View v : mTagCardViews) {
            viewGroup.addView(v);
        }
    }
}
