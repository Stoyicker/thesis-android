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

    public void setNavigationDrawerCallbacks(INavigationDrawerCallback callback) {
        mNavigationDrawerCallbacks = callback;
    }

    @Override
    public NavigationDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout
                .list_item_navigation_drawer, viewGroup, Boolean.FALSE);
        return new ViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(NavigationDrawerAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.textView.setText(mData.get(i).getText());
        viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds(mData.get(i)
                .getStandardDrawable(), null, null, null);

        if (mSelectedPosition == i) {
            viewHolder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color
                    .navigation_drawer_entry_background_selected));
        } else if (mTouchedPosition == i) {
            viewHolder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color
                    .navigation_drawer_entry_background_hovered));
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

    public static class ViewHolder extends RecyclerView.ViewHolder implements View
            .OnClickListener, View.OnTouchListener, View.OnLongClickListener {
        public TextView textView;
        private NavigationDrawerAdapter mAdapter;

        public ViewHolder(View itemView, NavigationDrawerAdapter adapter) {
            super(itemView);
            mAdapter = adapter;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnTouchListener(this);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }

        @Override
        public void onClick(View v) {
            final Integer pos = getPosition();
            if (mAdapter.mNavigationDrawerCallbacks != null) {
                if (mAdapter.getItemCount() - 1 != pos)
                    mAdapter.mNavigationDrawerCallbacks
                            .onNavigationTagGroupSelected(pos);
                else
                    mAdapter.mNavigationDrawerCallbacks
                            .onNewGroupCreationRequested();
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mAdapter.touchPosition(getPosition());
                    return Boolean.FALSE;
                case MotionEvent.ACTION_CANCEL:
                    mAdapter.touchPosition(-1);
                    return Boolean.FALSE;
                case MotionEvent.ACTION_MOVE:
                    return Boolean.FALSE;
                case MotionEvent.ACTION_UP:
                    mAdapter.touchPosition(-1);
                    return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        public boolean onLongClick(View v) {
            final Integer pos = getPosition();
            if (mAdapter.mNavigationDrawerCallbacks != null) {
                if (mAdapter.getItemCount() - 1 != pos)
                    mAdapter.mNavigationDrawerCallbacks
                            .onNavigationTagGroupRemovalRequested(pos);
            }
            return Boolean.TRUE;
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
        void onNavigationTagGroupSelected(int position);

        void onNewGroupCreationRequested();

        void onNavigationTagGroupRemovalRequested(Integer pos);
    }
}