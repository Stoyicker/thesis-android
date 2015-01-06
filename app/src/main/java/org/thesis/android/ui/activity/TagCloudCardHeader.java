package org.thesis.android.ui.activity;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.thesis.android.R;

import it.gmariotti.cardslib.library.internal.CardHeader;

public class TagCloudCardHeader extends CardHeader {

    private String mTitle;
    private TextView mTextView;

    public TagCloudCardHeader(Context context) {
        super(context, R.layout.tag_cloud_header);
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public void setTitle(String title) {
        mTitle = title;
        if (mTextView != null)
            mTextView.setText(title);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        if (view != null && mTextView == null) {
            mTextView = (TextView) view.findViewById(R.id.title_view);
            if (mTextView != null && !TextUtils.isEmpty(mTitle))
                mTextView.setText(mTitle);
        }
    }
}
