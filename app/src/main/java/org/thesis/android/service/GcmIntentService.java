package org.thesis.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thesis.android.BuildConfig;
import org.thesis.android.dev.CLog;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.io.net.HTTPRequestsSingleton;
import org.thesis.android.receiver.GcmBroadcastReceiver;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

public class GcmIntentService extends IntentService {

    public GcmIntentService() {
        super(GcmIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();
        final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        final String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                final String tagName;
                CLog.i("Sync signal for id: " + (tagName = extras.getString("tag")));
                final SQLiteDAO instance = SQLiteDAO.getInstance();
                Long lastSyncEpoch = instance.getLastFetchedEpoch(tagName);
                lastSyncEpoch = lastSyncEpoch == -1 ? 0 : lastSyncEpoch;
                new MessageParsingTask().executeOnExecutor(Executors.newSingleThreadExecutor(),
                        lastSyncEpoch, tagName);
            } else
                CLog.w("Received GCM message with messageType " + messageType + "\n" + extras.toString());
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private static class MessageParsingTask extends AsyncTask<Object, Void, String> {

        private Long mLastSyncEpoch;
        private String mTagName;

        @Override
        protected String doInBackground(Object... params) {
            final Long mLastSyncEpoch = (Long) params[0];
            final String mTagName = (String) params[1];
            final String MESSAGE_SERVER_ADDR = BuildConfig.FILE_SERVER_ADDR;
            final String TAGS_SERVICE_PATH = "/tags";

            final Request messageIdListRequest = new Request.Builder().url(
                    HTTPRequestsSingleton.httpEncodeAndStringify(MESSAGE_SERVER_ADDR,
                            TAGS_SERVICE_PATH, "epoch=" + mLastSyncEpoch + "&tags=" + mTagName))
                    .get().build();

            final Response response = HTTPRequestsSingleton.getInstance().performRequest
                    (messageIdListRequest);

            if (response == null || response.code() != 200) {
                CLog.e("Null response for sync request of tag " + mTagName);
                return null;
            }

            final JSONObject jsonResp;
            final JSONArray jsonArray;
            final List<String> messageIds = new LinkedList<>();
            try {
                jsonResp = new JSONObject(response.body().string());
                jsonArray = jsonResp.getJSONArray("messages");
                for (Integer i = 0; i < jsonArray.length(); i++) {
                    messageIds.add(jsonArray.getJSONObject(i).getString("msgId"));
                }
            } catch (JSONException | IOException e) {
                CLog.wtf(e);
                return null;
            }

            CLog.i("Message ids received upon sync request: " + messageIds.toString());

            final SQLiteDAO instance = SQLiteDAO.getInstance();
            instance.addMessageIdsToTag(messageIds, mTagName);
            instance.setLastFetchedEpoch(mTagName, System.currentTimeMillis());

            return WordUtils.capitalizeFully(mTagName);
        }

        @Override
        protected void onPostExecute(@Nullable String tagName) {
            if (tagName != null) {
                //TODO Notify the recyclerview so that it is refreshed (THIS FIRST)
                //TODO Show notification if it's not mine (THIS SECOND)
            }
        }
    }
}
