package org.thesis.android.ui.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.thesis.android.R;

@SuppressLint("ViewConstructor") //They wouldn't be used anyway
public class TagCardView extends CardView {

    public TagCardView(Context context, String tagName) {
        super(context);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.custom_view_tag_card, this, Boolean.TRUE);
        ((TextView) v.findViewById(R.id.tag_name)).setText(tagName);

        super.setCardBackgroundColor(context.getResources().getColor(R.color
                .material_purple_900));
    }
}
