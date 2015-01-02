package org.thesis.android.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.thesis.android.R;

import java.util.List;

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter
        .ViewHolder> {

    private List<NavigationItem> mData;
    private INavigationDrawerCallback mNavigationDrawerCallbacks;
    private int mSelectedPosition;
    private int mTouchedPosition = -1;
    private Context mContext;

    public NavigationDrawerAdapter(Context context, List<NavigationItem> data) {
        mContext = context;
        mData = data;
    }

    public void setNavigationDrawerCallbacks(INavigationDrawerCallback INavigationDrawerCallback) {
        mNavigationDrawerCallbacks = INavigationDrawerCallback;
    }

    @Override
    public NavigationDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout
                .list_item_navigation_drawer, viewGroup, Boolean.FALSE);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NavigationDrawerAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.textView.setText(mData.get(i).getText());
        viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        //TODO Correct this and put wherever it proceeds
        viewHolder.itemView.setOnTouchListener(new View.OnTouchListener() {
                                                   @Override
                                                   public boolean onTouch(View v,
                                                                          MotionEvent event) {

                                                       switch (event.getAction()) {
                                                           case MotionEvent.ACTION_DOWN:
                                                               touchPosition(i);
                                                               return Boolean.FALSE;
                                                           case MotionEvent.ACTION_CANCEL:
                                                               touchPosition(-1);
                                                               return Boolean.FALSE;
                                                           case MotionEvent.ACTION_MOVE:
                                                               return Boolean.FALSE;
                                                           case MotionEvent.ACTION_UP:
                                                               touchPosition(-1);
                                                               return Boolean.FALSE;
                                                       }
                                                       return Boolean.TRUE;
                                                   }
                                               }
        );

        //TODO Correct this and put wherever it proceeds
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View v) {
                                                       if (mNavigationDrawerCallbacks != null)
                                                           mNavigationDrawerCallbacks
                                                                   .onNavigationDrawerItemSelected(i);
                                                   }
                                               }
        );

        viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds(mData.get(i)
                .getStandardDrawable(), null, null, null);

        if (mSelectedPosition == i || mTouchedPosition == i) {
            viewHolder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color
                    .navigation_drawer_entry_background_selected));
        } else {
            viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void touchPosition(int position) {
        int lastPosition = mTouchedPosition;
        mTouchedPosition = position;
        if (lastPosition >= 0)
            notifyItemChanged(lastPosition);
        if (position >= 0)
            notifyItemChanged(position);
    }

    public Integer getSelectedPosition() {
        return mSelectedPosition;
    }

    public void selectPosition(int position) {
        int lastPosition = mSelectedPosition;
        mSelectedPosition = position;
        notifyItemChanged(lastPosition);
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    public static class NavigationItem {
        private final String mText;
        private final Drawable mStandardDrawable;

        public NavigationItem(String text, Drawable standardDrawable) {
            mText = text;
            mStandardDrawable = standardDrawable;
        }

        public String getText() {
            return mText;
        }

        public Drawable getStandardDrawable() {
            return mStandardDrawable;
        }
    }

    public interface INavigationDrawerCallback {
        void onNavigationDrawerItemSelected(int position);
    }
}