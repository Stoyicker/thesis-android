package org.thesis.android.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private Integer mVisibleItems = 20;

    public MessageListAdapter(Context context, View emptyView) {
        mContext = context;
        mEmptyView = emptyView;
    }

    @Override
    public MessageListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .list_item_message, parent, Boolean.FALSE);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageListAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
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
            notifyDataSetChanged();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
