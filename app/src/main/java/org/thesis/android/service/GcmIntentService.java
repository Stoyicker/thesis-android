package org.thesis.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.thesis.android.devutil.CLog;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.receiver.GcmBroadcastReceiver;

import java.util.Arrays;
import java.util.concurrent.Executors;

public class GcmIntentService extends IntentService {

    public GcmIntentService() {
        super(GcmIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();
        final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        if (!extras.isEmpty()) {
            final String tagName;
            CLog.i("Sync requested by gcm for tag: " + (tagName = extras.getString("tag")));
            final SQLiteDAO instance = SQLiteDAO.getInstance();
            Long lastSyncEpoch = instance.getLastFetchedEpoch(tagName);
            lastSyncEpoch = lastSyncEpoch == -1 ? 0 : lastSyncEpoch;
            new MessageParsingTask(getApplicationContext()).executeOnExecutor(Executors
                            .newSingleThreadExecutor(),
                    lastSyncEpoch, Arrays.asList(tagName));
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}
