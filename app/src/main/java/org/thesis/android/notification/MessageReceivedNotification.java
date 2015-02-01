package org.thesis.android.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import org.thesis.android.R;
import org.thesis.android.ui.activity.NavigationDrawerActivity;

public final class MessageReceivedNotification {

    private static volatile MessageReceivedNotification mInstance;
    private Integer mId;
    private static final Object LOCK = new Object();

    public static MessageReceivedNotification getInstance() {
        MessageReceivedNotification ret = mInstance;
        if (ret == null)
            synchronized (LOCK) {
                ret = mInstance;
                if (ret == null) {
                    ret = new MessageReceivedNotification();
                    mInstance = ret;
                }
            }
        return ret;
    }

    private MessageReceivedNotification() {
        mId = -1;
    }

    public synchronized void show(final @NonNull Context context) {
        if (mId != -1)
            return;
        final String title = context.getString(R.string.app_name);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        final Integer id = 0; //Beware, this only works because the notification is unique across
        // the whole app

        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder.setSmallIcon(R.drawable.ic_stat_unread);
        builder.setContentTitle(title);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(
                R.string.new_messages_notification)));
        builder.setAutoCancel(Boolean.TRUE);
        final Intent resultIntent = new Intent(context, NavigationDrawerActivity.class);
        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        final PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_ONE_SHOT
                );
        builder.setContentIntent(resultPendingIntent);
        notificationManager.notify(mId = id, builder.build());
    }

    public synchronized void dismiss(final @NonNull Context context) {
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mId != -1) {
            notificationManager.cancel(mId);
            mId = -1;
        }
    }
}