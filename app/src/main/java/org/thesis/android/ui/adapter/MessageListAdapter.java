package org.thesis.android.ui.adapter;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thesis.android.BuildConfig;
import org.thesis.android.R;
import org.thesis.android.datamodel.MessageWrapper;
import org.thesis.android.devutil.CLog;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.io.net.HTTPRequestsSingleton;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private final List<MessageWrapper> mData = new LinkedList<>();
    private final Collection<String> mTags;
    private final View mEmptyView;
    private final SwipeRefreshLayout mSwipeRefreshLayout;
    private static final Integer NEW_ITEM_BATCH_AMOUNT = 5;
    private Integer mVisibleItems = 10;

    private enum ItemType {
        WITH_ATTACHMENTS,
        WITHOUT_ATTACHMENTS
    }

    public MessageListAdapter(@NonNull final View emptyView,
                              @NonNull final Collection<String> tags,
                              @NonNull final SwipeRefreshLayout swipeRefreshLayout) {
        mEmptyView = emptyView;
        mTags = tags;
        mSwipeRefreshLayout = swipeRefreshLayout;
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
        new MessageDownloaderTask(this, mTags, mData, mSwipeRefreshLayout).executeOnExecutor
                (Executors.newSingleThreadExecutor());
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
        private final View attachmentImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            circleView = (TextView) itemView.findViewById(R.id.circle_view);
            senderView = (TextView) itemView.findViewById(R.id.sender_view);
            bodyView = (TextView) itemView.findViewById(R.id.body_view);
            attachmentImageView = itemView.findViewById(R.id.attachment_view);
            if (attachmentImageView != null)
                attachmentImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO Attachment download onClickListener with DownloadManager
                    }
                });
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
            //TODO List item onClickListener
        }
    }

    private static class MessageDownloaderTask extends AsyncTask<Void, Void, Collection<MessageWrapper>> {

        private final MessageListAdapter mAdapter;
        private final Collection<MessageWrapper> mAdapterData;
        private final Collection<String> mTags;
        private final SwipeRefreshLayout mRefreshLayout;

        private MessageDownloaderTask(@NonNull final MessageListAdapter adapter,
                                      @NonNull final Collection<String> tags,
                                      @NonNull final Collection<MessageWrapper> adapterData,
                                      @NonNull final SwipeRefreshLayout swipeRefreshLayout) {
            mAdapter = adapter;
            mTags = tags;
            mAdapterData = adapterData;
            mRefreshLayout = swipeRefreshLayout;
        }

        @Override
        protected synchronized Collection<MessageWrapper> doInBackground(Void... params) {
            List<MessageWrapper> messages = new LinkedList<>();
            final SQLiteDAO sqLiteDAO = SQLiteDAO.getInstance();
            for (final String tag : mTags) {
                final Collection<String> idsInThisTag = sqLiteDAO.getTagMessageIds(tag);
                for (final String id : idsInThisTag) {
                    final Request messageRequest = new Request.Builder().get().url
                            (HTTPRequestsSingleton.httpEncodeAndStringify(BuildConfig
                                            .FILE_SERVER_ADDR,
                                    "/messages",
                                    "messageid=" + id)).build();

                    final Response response = HTTPRequestsSingleton.getInstance().performRequest
                            (messageRequest);

                    if (response != null && response.isSuccessful()) {
                        try {

                            final JSONObject object = new JSONObject(response.body().string());
                            final JSONArray array = object.getJSONArray("tags");
                            final String[] tags = new String[array.length()];
                            for (int i = 0; i < array.length(); i++) {
                                tags[i] = array.getString(i);
                            }
                            String regularBody = null, sketchBoard = null;
                            if (object.getBoolean("has_Normal")) {
                                regularBody = object.getString("content_html");
                            }
                            if (object.getBoolean("has_SketchBoard")) {
                                sketchBoard = object.getString("sketchboard_content_html");
                            }

                            if (regularBody == null && sketchBoard == null)
                                throw new JSONException("No body found, of any types.");

                            messages.add(new MessageWrapper(object.getString("sender"),
                                    regularBody != null ? regularBody : sketchBoard,
                                    object.getBoolean("has_attachments"),
                                    tags));
                        } catch (JSONException | IOException e) {
                            CLog.wtf(e);
                        }
                    }
                }
            }
            Collections.reverse(messages);
            return messages;
        }

        @Override
        protected void onPostExecute(@NonNull final Collection<MessageWrapper> messages) {
            if (!messages.isEmpty()) {
                mAdapterData.clear();
                mAdapterData.addAll(messages);
                mAdapter.refreshEmptyViewVisibility();
                mAdapter.notifyDataSetChanged();
            }
            mRefreshLayout.setRefreshing(Boolean.FALSE);
        }
    }
}
