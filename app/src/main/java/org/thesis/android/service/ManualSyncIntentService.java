package org.thesis.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import org.thesis.android.devutil.CLog;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.receiver.GcmBroadcastReceiver;
import org.thesis.android.ui.fragment.MessageListContainerFragment;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

public class ManualSyncIntentService extends IntentService {

    public ManualSyncIntentService() {
        super(ManualSyncIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();

        if (!extras.isEmpty()) {
            final List<String> tagList;
            CLog.i("Sync manually requested for tags: " + (tagList = extras.getStringArrayList
                    (MessageListContainerFragment.EXTRA_KEY_TAG_LIST)));
            final SQLiteDAO instance = SQLiteDAO.getInstance();
            final List<Long> epochs = new LinkedList<>();
            for (String tag : tagList) {
                epochs.add(instance.getLastFetchedEpoch(tag));
            }
            Long lastSyncEpoch;
            if (!epochs.isEmpty()) {
                lastSyncEpoch = Collections.min(epochs);
            } else
                lastSyncEpoch = -1L;
            lastSyncEpoch = lastSyncEpoch == -1 ? 0 : lastSyncEpoch;
            new MessageParsingTask(getApplicationContext()).executeOnExecutor(Executors
                            .newSingleThreadExecutor(),
                    lastSyncEpoch, tagList);

        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}
