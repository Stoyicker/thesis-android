package org.thesis.android.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thesis.android.BuildConfig;
import org.thesis.android.devutil.CLog;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.io.net.HTTPRequestsSingleton;
import org.thesis.android.notification.MessageReceivedNotification;
import org.thesis.android.ui.fragment.MessageListContainerFragment;
import org.thesis.android.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class MessageParsingTask extends AsyncTask<Object, Void, ArrayList<String>> {

    private Context mContext;
    private Boolean mIsRunningOnForeground;
    private final List<String> mNewIds = new LinkedList<>();

    public MessageParsingTask(Context context) {
        mContext = context;
    }

    @Override
    protected ArrayList<String> doInBackground(Object... params) {
        final Long mLastSyncEpoch = (Long) params[0];
        //noinspection unchecked
        final List<String> tags = (List<String>) params[1];
        final String MESSAGE_SERVER_ADDR = BuildConfig.FILE_SERVER_ADDR;
        final String TAGS_SERVICE_PATH = "/tags";
        final ArrayList<String> ret = new ArrayList<>();

        for (String tag : tags) {
            final Request messageIdListRequest = new Request.Builder().url(
                    HTTPRequestsSingleton.httpEncodeAndStringify(MESSAGE_SERVER_ADDR,
                            TAGS_SERVICE_PATH, "epoch=" + mLastSyncEpoch, "tags=" + tag))
                    .get().build();

            final Response response = HTTPRequestsSingleton.getInstance().performRequest
                    (messageIdListRequest);

            if (response == null || response.code() != 200) {
                CLog.e("Null response for sync request of tag " + tag);
                return null;
            }

            final JSONObject jsonResp;
            final JSONArray jsonArray;
            try {
                jsonResp = new JSONObject(response.body().string());
                jsonArray = jsonResp.getJSONArray("messages");
                for (Integer i = 0; i < jsonArray.length(); i++) {
                    mNewIds.add(jsonArray.getJSONObject(i).getString("msgId"));
                }
            } catch (JSONException | IOException e) {
                CLog.wtf(e);
                return null;
            }

            CLog.i("Message ids received upon sync request: " + mNewIds.toString());

            final SQLiteDAO instance = SQLiteDAO.getInstance();
            instance.addMessageIdsToTag(mNewIds, tag);
            instance.setLastFetchedEpoch(tag, System.currentTimeMillis());

            ret.add(WordUtils.capitalizeFully(tag));
        }

        mIsRunningOnForeground = Utils.isRunningOnForeground(mContext);

        return ret;
    }

    @Override
    protected void onPostExecute(@Nullable ArrayList<String> tags) {
        broadcastSyncDone(tags);
        if (tags != null) {
            if (!tags.isEmpty() && !mIsRunningOnForeground) {
                final SQLiteDAO instance = SQLiteDAO.getInstance();
                for (final String id : mNewIds) {
                    if (!instance.isMessageIdMine(id)) {
                        MessageReceivedNotification.getInstance().show(mContext);
                        break;
                    }
                }
            }
        }
    }

    //TODO Fix a bug where tags that have been subscribed to once can't be re-subscribed to
    //TODO Fix a bug where the messages are not being refreshed when a tag is supressed (or at
    // least when going from tag L to no tags)

    public void broadcastSyncDone(ArrayList<String> tags) {
        Intent intent = new Intent();
        intent.setAction("org.thesis.android.SYNC_DONE");
        intent.putStringArrayListExtra(MessageListContainerFragment.EXTRA_KEY_TAG_LIST, tags);
        LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
    }
}
