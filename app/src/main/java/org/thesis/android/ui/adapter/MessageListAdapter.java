package org.thesis.android.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

    private final List<String> mData = new LinkedList<>();
    private final View mEmptyView;
    private final Context mContext;
    private Integer visibleItems;

    public MessageListAdapter(Context context, View emptyView) {
        mContext = context;
        mEmptyView = emptyView;
    }

    @Override
    public NavigationDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(NavigationDrawerAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public void showNextItemBatch() {

    }

    public void requestDataLoad() {

    }

    private static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
