package org.thesis.android.ui.card.tag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;

import org.thesis.android.R;

@SuppressLint("ViewConstructor") //They wouldn't be used anyway
public class AddedTagCardView extends CardView implements ITagCard, View.OnLongClickListener {

    private TagCardView.ITagRemovalListener mCallback;
    private String mTagName = null;

    public AddedTagCardView(Context context, TagCardView.ITagRemovalListener _removalCallback) {
        super(context);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);

        mInflater.inflate(R.layout.custom_view_added_tag_card, this, Boolean.TRUE);

        mCallback = _removalCallback;

        super.setCardBackgroundColor(context.getResources().getColor(R.color
                .material_deep_purple_900));

        setOnLongClickListener(this);

        //TODO On EditText focus lost, notify the (yet-to-do) listener which will dump the data
        // to the database, disable both focusmodes from the EditText, forbid it from being editable
        // and set to FALSE the (yet-to-do) global isBeingEdited field
    }

    @Override
    public boolean onLongClick(View v) {
        //FIXME Only do this if the global field isBeingEdited is false
        v.setVisibility(View.GONE);
        if (mCallback != null && v instanceof AddedTagCardView)
            mCallback.onTagRemoved(this);
        return Boolean.TRUE;
    }

    @Override
    public String getName() {
        return mTagName;
    }
}
