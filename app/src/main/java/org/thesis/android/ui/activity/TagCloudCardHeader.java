package org.thesis.android.ui.activity;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.thesis.android.R;

import it.gmariotti.cardslib.library.internal.CardHeader;

public class TagCloudCardHeader extends CardHeader {

    private String mCustomTitle;

    public TagCloudCardHeader(Context context) {
        super(context, R.layout.tag_cloud_header);
    }

    public void setCustomTitle(String title) {
        mCustomTitle = title;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        if (view == null) return;
        final TextView customTextView = (TextView) view.findViewById(R.id.title_view);

        if (customTextView != null)
            customTextView.setText(mCustomTitle);
    }
}
