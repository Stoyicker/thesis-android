package org.thesis.android.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.thesis.android.R;
import org.thesis.android.datamodel.MessageWrapper;
import org.thesis.android.io.database.SQLiteDAO;

import java.util.LinkedList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private final List<MessageWrapper> mData = new LinkedList<>();
    private final List<String> mTags;
    private final View mEmptyView;
    private final Object ADAPTER_RELOAD_LOCK = new Object();
    private static final Integer NEW_ITEM_BATCH_AMOUNT = 5;
    private Integer mVisibleItems = 10;

    private enum ItemType {
        WITH_ATTACHMENTS,
        WITHOUT_ATTACHMENTS
    }

    public MessageListAdapter(@NonNull final View emptyView,
                              @NonNull final List<String> tags) {
        mEmptyView = emptyView;
        mTags = tags;
        requestDataLoad();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v;
        final Integer resId;

        if (viewType == ItemType.WITH_ATTACHMENTS.ordinal())
            resId = R.layout.list_item_message_with_attachments;
        else if (viewType == ItemType.WITHOUT_ATTACHMENTS.ordinal())
            resId = R.layout.list_item_message_without_attachments;
        else
            throw new IllegalArgumentException("Unexpected viewType " + viewType);

        v = LayoutInflater.from(parent.getContext()).inflate(resId, parent, Boolean.FALSE);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final MessageWrapper item = mData.get(position);
        if (item != null) {
            holder.setSender(item.getSender());
            holder.setBody(item.getBody());
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(mVisibleItems, mData.size());
    }

    @Override
    public int getItemViewType(int position) {
        final MessageWrapper item = mData.get(position);
        if (item != null)
            if (item.hasAttachments()) {
                return ItemType.WITH_ATTACHMENTS.ordinal();
            } else
                return ItemType.WITHOUT_ATTACHMENTS.ordinal();
        return -1;
    }

    public void showNextItemBatch() {
        mVisibleItems += NEW_ITEM_BATCH_AMOUNT;
        requestDataLoad();
    }

    public void requestDataLoad() {
        synchronized (ADAPTER_RELOAD_LOCK) {
            mData.clear();
            for (String x : mTags)
                mData.addAll(SQLiteDAO.getInstance().getTagMessages(x));
            refreshEmptyViewVisibility();
            notifyDataSetChanged();
        }
    }

    private void refreshEmptyViewVisibility() {
        mEmptyView.setVisibility(getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    public void addTagAndRefresh(String name) {
        mTags.add(name);
        requestDataLoad();
    }

    public void removeTagAndRefresh(String name) {
        mTags.remove(name);
        requestDataLoad();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View
            .OnClickListener {

        private final TextView circleView, senderView, bodyView;

        public ViewHolder(View itemView) {
            super(itemView);
            circleView = (TextView) itemView.findViewById(R.id.circle_view);
            senderView = (TextView) itemView.findViewById(R.id.sender_view);
            bodyView = (TextView) itemView.findViewById(R.id.body_view);
        }

        private void setSender(@NonNull final String senderName) {
            if (TextUtils.isEmpty(senderName))
                return;
            //Without the addendum it'll look for a resource instead
            circleView.setText(senderName.charAt(0) + "");
            senderView.setText(senderName);
        }

        private void setBody(@NonNull final String messageBody) {
            bodyView.setText(messageBody);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
