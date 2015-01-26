package org.thesis.android.io.net;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.thesis.android.BuildConfig;
import org.thesis.android.io.prefs.PreferenceAssistant;
import org.thesis.android.ui.component.tag.ITagCard;
import org.thesis.android.ui.component.tag.TagCardView;
import org.thesis.android.util.Utils;

import java.util.concurrent.Executors;

public abstract class SubscriptionManager {

    private enum SubscriptionOperationType {
        SUBSCRIBE,
        UNSUBSCRIBE
    }

    public static void subscribeToTagInBackground(Context context, TagCardView tag,
                                                  ISubscriptionListener listener) {
        new SubscriptionOperationTask().executeOnExecutor(Executors
                .newSingleThreadExecutor(), tag, listener, context, SubscriptionOperationType.SUBSCRIBE);
    }

    public static void unsubscribeFromTagInBackground(Context context, ITagCard tag,
                                                      IUnsubscriptionListener listener) {
        new SubscriptionOperationTask().executeOnExecutor(Executors
                        .newSingleThreadExecutor(), tag, listener, context,
                SubscriptionOperationType.UNSUBSCRIBE);
    }

    public interface ISubscriptionListener {
        public void onSubscriptionAttemptFinished(TagCardView tag, Boolean success);
    }

    public interface IUnsubscriptionListener {
        public void onUnsubscriptionAttemptFinished(ITagCard tag, Boolean success);
    }

    private static class SubscriptionOperationTask extends AsyncTask<Object, Void, Boolean> {

        private ISubscriptionListener mSubscriptionCallback;
        private IUnsubscriptionListener mUnsubscriptionCallback;
        private ITagCard mTag;
        private Context mContext;
        private SubscriptionOperationType mOperationType;

        @Override
        protected Boolean doInBackground(Object... params) {
            mTag = (ITagCard) params[0];
            mContext = (Context) params[2];
            mOperationType = (SubscriptionOperationType) params[3];
            switch (mOperationType) {
                case SUBSCRIBE:
                    mSubscriptionCallback = (ISubscriptionListener) params[1];
                    break;
                case UNSUBSCRIBE:
                    mUnsubscriptionCallback = (IUnsubscriptionListener) params[1];
                    break;
                default:
                    throw new IllegalArgumentException("Unknown subscription operation type " +
                            "" + mOperationType);
            }
            if (!Utils.isInternetReachable())
                return Boolean.FALSE;

            final String deviceId = PreferenceAssistant.readSharedString(mContext,
                    PreferenceAssistant.PROPERTY_REG_ID, "");

            if (TextUtils.isEmpty(deviceId))
                return Boolean.FALSE;

            final Request request = new Request.Builder().url(HTTPRequestsSingleton.httpEncodeAndStringify(
                    BuildConfig.GCM_SERVER_ADDR,
                    "/tags",
                    "type=" + (mOperationType == SubscriptionOperationType.SUBSCRIBE ?
                            "subscribe" : "unsubscribe") + "&tags=" + mTag.getName() + "&id=" + deviceId))
                    .post(null).build();

            final Response response = HTTPRequestsSingleton.getInstance()
                    .performRequest(request);

            return response != null && response.isSuccessful();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            switch (mOperationType) {
                case SUBSCRIBE:
                    if (mSubscriptionCallback != null)
                        mSubscriptionCallback.onSubscriptionAttemptFinished((TagCardView) mTag,
                                success);
                    break;
                case UNSUBSCRIBE:
                    if (mUnsubscriptionCallback != null)
                        mUnsubscriptionCallback.onUnsubscriptionAttemptFinished(mTag, success);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown subscription operation type " +
                            "" + mOperationType);
            }
        }
    }
}
