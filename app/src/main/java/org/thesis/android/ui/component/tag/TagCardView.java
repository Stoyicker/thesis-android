package org.thesis.android.ui.component.tag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.thesis.android.R;

@SuppressLint("ViewConstructor") //They wouldn't be used anyway
public class TagCardView extends CardView implements ITagCard, View.OnClickListener {

    private ITagChangedListener mCallback;
    private final String mTagName;

    public TagCardView(Context context, String tagName, ITagChangedListener _callback) {
        super(context);
        View v = ((LayoutInflater) context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_view_tag_card, this,
                Boolean.TRUE);
        ((TextView) v.findViewById(R.id.tag_name)).setText(tagName);
        mCallback = _callback;
        mTagName = tagName;

        super.setCardBackgroundColor(context.getResources().getColor(R.color
                .material_purple_900));

        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        v.setVisibility(View.GONE);
        if (mCallback != null && v instanceof TagCardView)
            mCallback.onTagRemoved(this);
    }

    @Override
    public String getName() {
        return mTagName;
    }
}
