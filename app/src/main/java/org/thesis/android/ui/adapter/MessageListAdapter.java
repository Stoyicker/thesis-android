package org.thesis.android.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.thesis.android.R;
import org.thesis.android.io.database.SQLiteDAO;

import java.util.LinkedList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private final List<String> mData = new LinkedList<>();
    private final View mEmptyView;
    private final Context mContext;
    private final Object ADAPTER_RELOAD_LOCK = new Object();
    private static final Integer NEW_ITEM_BATCH_AMOUNT = 5;
    private Integer mVisibleItems = 10;

    public MessageListAdapter(Context context, View emptyView) {
        mContext = context;
        mEmptyView = emptyView;
        requestDataLoad();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .list_item_message, parent, Boolean.FALSE);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String item = mData.get(position);
        if (item != null) {
            holder.setSender(item);
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(mVisibleItems, mData.size());
    }

    public void showNextItemBatch() {
        mVisibleItems += NEW_ITEM_BATCH_AMOUNT;
        requestDataLoad();
    }

    public void requestDataLoad() {
        synchronized (ADAPTER_RELOAD_LOCK) {
            mData.clear();
            //TODO This should retrieve the messages instead
            mData.addAll(SQLiteDAO.getInstance().getGroupTags("Ungrouped"));
            refreshEmptyViewVisibility();
            notifyDataSetChanged();
        }
    }

    private void refreshEmptyViewVisibility() {
        mEmptyView.setVisibility(getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    public void addTagAndRefresh(String name) {
        mData.add(name);
        requestDataLoad();
    }

    public void removeTagAndRefresh(String name) {
        mData.remove(name);
        requestDataLoad();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View
            .OnClickListener {

        private final TextView circleView, senderView;

        public ViewHolder(View itemView) {
            super(itemView);
            circleView = (TextView) itemView.findViewById(R.id.circle_view);
            senderView = (TextView) itemView.findViewById(R.id.sender_view);
        }

        private void setSender(@NonNull final String senderName) {
            if (TextUtils.isEmpty(senderName))
                return;
            //Without the addendum it'll look for a resource instead
            circleView.setText(senderName.charAt(0) + "");
            senderView.setText(senderName);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
