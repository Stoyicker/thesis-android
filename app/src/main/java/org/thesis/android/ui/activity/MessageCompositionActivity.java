package org.thesis.android.ui.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thesis.android.BuildConfig;
import org.thesis.android.CApplication;
import org.thesis.android.R;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.io.net.HTTPRequestsSingleton;
import org.thesis.android.io.prefs.PreferenceAssistant;
import org.thesis.android.ui.component.FlowLayout;
import org.thesis.android.ui.component.attachment.AttachmentView;
import org.thesis.android.ui.component.tag.AddedTagCardView;
import org.thesis.android.ui.component.tag.ITagCard;
import org.thesis.android.ui.component.tag.TagCardView;
import org.thesis.android.ui.dialog.FileSelectorDialog;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class MessageCompositionActivity extends ActionBarActivity implements ITagCard
        .ITagChangedListener, FileSelectorDialog.IOnFolderSelectionListener,
        AttachmentView.IOnAttachmentRemovedListener {

    public static final String EXTRA_TAG = "EXTRA_TAG";
    private final List<ITagCard> mTags = new LinkedList<>();
    private FlowLayout mFlowLayout;
    private Context mContext;
    private SlidingUpPanelLayout mSlidingPaneLayout;
    private View mEmptyTagsView;
    private FlowLayout mAttachmentContainer;
    private EditText mEditTextView;
    private List<File> mAttachments = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_composition);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setTitle(R.string.action_compose);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);

        mContext = CApplication.getInstance().getContext();

        mEmptyTagsView = findViewById(android.R.id.empty);

        mEditTextView = (EditText) findViewById(R.id.message_body);

        toolbar.findViewById(R.id.action_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        tryToSendCurrentMessage();
                    }
                });
            }
        });

        toolbar.findViewById(R.id.action_attach).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        showAddAttachmentDialog();
                    }
                });
            }
        });

        mSlidingPaneLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mFlowLayout = (FlowLayout) mSlidingPaneLayout.findViewById(R.id.tag_container);
        mAttachmentContainer = (FlowLayout) findViewById(R.id.attachment_container);

        mFlowLayout.setOnTouchListener(new View.OnTouchListener() {

            public static final float CLICK_ACTION_THRESHOLD = 5;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        if (isClick(startX, endX, startY, endY) && mSlidingPaneLayout
                                .isPanelExpanded()) {
                            if (mEmptyTagsView.isShown())
                                mEmptyTagsView.setVisibility(View.GONE);
                            for (ITagCard c : mTags) {
                                if (!(c instanceof AddedTagCardView)) continue;
                                final AddedTagCardView castedC = (AddedTagCardView) c;
                                if (castedC.isBeingBuilt()) {
                                    ((AddedTagCardView) c).cancelTagCreation();
                                    return Boolean.TRUE;
                                }
                            }
                            final AddedTagCardView tag;
                            mFlowLayout.addView(tag = new AddedTagCardView(mContext,
                                    MessageCompositionActivity.this,
                                    findViewById(android.R.id.content), Boolean.FALSE));
                            mTags.add(tag);
                            return Boolean.TRUE;
                        }
                        break;
                }
                return Boolean.FALSE;
            }

            private boolean isClick(float startX, float endX, float startY, float endY) {
                float differenceX = Math.abs(startX - endX);
                float differenceY = Math.abs(startY - endY);
                return !(differenceX > CLICK_ACTION_THRESHOLD || differenceY >
                        CLICK_ACTION_THRESHOLD);
            }
        });

        updateEmptyViewVisibility();
    }

    private void showAddAttachmentDialog() {
        new FileSelectorDialog().show(this);
    }

    @Override
    public void onTagCreated(ITagCard tag) {
    }

    @Override
    public void onTagAdded(ITagCard tag) {
        mTags.remove(tag);
        mFlowLayout.removeView((View) tag);
        final TagCardView v = new TagCardView(mContext, WordUtils.capitalizeFully(tag.getName()),
                this);
        if (!mTags.contains(v)) {
            mTags.add(v);
            mFlowLayout.addView(v);
        } else {
            Toast.makeText(mContext, R.string.send_duplicated_tag,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onTagRemoved(ITagCard tag) {
        mTags.remove(tag);
        mFlowLayout.removeView((View) tag);
        updateEmptyViewVisibility();
    }

    private void updateEmptyViewVisibility() {
        if (mFlowLayout.getChildCount() == 1 && !(mFlowLayout.getChildAt(0) instanceof ITagCard))
            mEmptyTagsView.setVisibility(View.VISIBLE);
        else
            mEmptyTagsView.setVisibility(View.GONE);
    }

    @Override
    public void onTagCreationCancelled(ITagCard tag) {
        mTags.remove(tag);
        mFlowLayout.removeView((View) tag);
        updateEmptyViewVisibility();
    }

    private void tryToSendCurrentMessage() {
        if (mTags.isEmpty()) {
            Toast.makeText(mContext, R.string.send_error_no_targets, Toast.LENGTH_LONG).show();
            return;
        }
        final String messageBody = mEditTextView.getText().toString();
        if (TextUtils.isEmpty(messageBody) && mAttachments.isEmpty()) {
            Toast.makeText(mContext, R.string.send_error_nothing_to_send, Toast.LENGTH_LONG).show();
            return;
        }
        if (!TextUtils.isEmpty(messageBody) && Pattern.matches(getString(R.string
                .attachment_included_pattern), messageBody) && mAttachments.isEmpty()) {
            new MaterialDialog.Builder(this)
                    .title(R.string.warning)
                    .positiveText(R.string.send_anyway)
                    .negativeText(android.R.string.cancel)
                    .titleColor(R.color.material_purple_900)
                    .positiveColorRes(R.color.material_purple_900)
                    .negativeColorRes(R.color.material_purple_900)
                    .backgroundColor(android.R.color.white)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog materialDialog) {
                            MessageCompositionActivity.this.sendCurrentMessage(messageBody,
                                    mAttachments);
                        }
                    })
                    .show();
        } else
            sendCurrentMessage(messageBody, mAttachments);
    }

    private void sendCurrentMessage(final String messageBody, List<File> attachments) {
        new AsyncTask<Object, Void, Boolean>() {

            private Toast mSendingToast;
            private String mMessageBody;
            private List<File> mMessageAttachments;
            private List<ITagCard> mTagList;

            @Override
            protected void onPreExecute() {
                mSendingToast = Toast.makeText(MessageCompositionActivity.this.mContext,
                        R.string.message_send_requested, Toast.LENGTH_SHORT);
                mSendingToast.show();
            }

            @Override
            protected Boolean doInBackground(Object... params) {
                final String MESSAGE_SERVER_ADDR = BuildConfig.FILE_SERVER_ADDR;
                final String MESSAGE_BODY_SERVICE_PATH = "/messages";
                final String MESSAGE_ATTACHMENTS_SERVICE_PATH = "/files";
                mMessageBody = (String) params[0];
                //noinspection unchecked
                mMessageAttachments = (List<File>) params[1];
                //noinspection unchecked
                mTagList = (List<ITagCard>) params[2];

                final JSONObject bodyContents = new JSONObject();

                try {
                    bodyContents.put("sender", PreferenceAssistant.readSharedString(MessageCompositionActivity.this.mContext,
                            PreferenceAssistant.PREF_USER_NAME, null));
                    bodyContents.put("content_html", mMessageBody);
                    final JSONArray tagsContainer = new JSONArray();
                    for (ITagCard t : mTagList)
                        tagsContainer.put(t.getName());
                    bodyContents.put("tags", tagsContainer);
                } catch (JSONException e) {
                    Log.wtf("debug", e);
                    return Boolean.FALSE;
                }

                final RequestBody body = RequestBody.create(MediaType.parse("application/json; " +
                        "charset=UTF-8"), bodyContents.toString());
                final Request messageBodyRequest = new Request.Builder().url(MESSAGE_SERVER_ADDR + MESSAGE_BODY_SERVICE_PATH)
                        .post(body).build();

                final Response bodyResponse = HTTPRequestsSingleton.getInstance().performRequest
                        (messageBodyRequest);

                if (bodyResponse == null || bodyResponse.code() !=
                        HTTPRequestsSingleton
                                .SC_OK)
                    return Boolean.FALSE;

                JSONObject jsonResp;
                String messageId;

                try {
                    jsonResp = new JSONObject(bodyResponse.body().string());
                    if (!jsonResp.getString
                            ("status").contentEquals("ok")) {
                        return Boolean.FALSE;
                    }
                    messageId = jsonResp.getString("msgid");
                } catch (JSONException | IOException e) {
                    Log.wtf("debug", e);
                    return Boolean.FALSE;
                }

                //TODO Process body response, send attachments if any and report success on return
                //Processing the response includes
                SQLiteDAO.getInstance().markMessageIdAsMine(messageId);
                // -Subscribing to the topic (should send id in the bodyRequest)
                // -Store subscription in "Uncategorized" if it's new

                return Boolean.TRUE; //Success of the sending
            }

            @Override
            protected void onPostExecute(Boolean success) {
                final Integer resId = success ? R.string.message_sent_status_ok : R.string
                        .message_sent_status_err;
                mSendingToast.cancel();
                Toast.makeText(MessageCompositionActivity.this.mContext, resId,
                        Toast.LENGTH_SHORT).show();
                if (success)
                    MessageCompositionActivity.this.requestActivityReturn();
            }
        }.executeOnExecutor(Executors.newSingleThreadExecutor(), messageBody, attachments, mTags);
    }

    private void requestActivityReturn() {
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(R.anim.move_in_from_bottom, R.anim.move_out_to_bottom);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                requestActivityReturn();
                return Boolean.TRUE;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Boolean consumed = Boolean.FALSE;

        AddedTagCardView viewToRemove = null;
        for (ITagCard c : mTags) {
            if (!(c instanceof AddedTagCardView)) continue;
            final AddedTagCardView castedC = (AddedTagCardView) c;
            if (castedC.isBeingBuilt()) {
                viewToRemove = castedC;
                break;
            }
        }
        if (viewToRemove == null) {
            if (mSlidingPaneLayout.isPanelExpanded()) {
                mSlidingPaneLayout.collapsePanel();
                consumed = Boolean.TRUE;
            }
        } else {
            viewToRemove.cancelTagCreation(); //Discard the added tag
            consumed = Boolean.TRUE;
        }

        if (!consumed)
            super.onBackPressed();
    }

    @Override
    public void onFileSelection(File file) {
        if (file.isDirectory()) {
            Toast.makeText(mContext, R.string.attach_error_file_is_directory,
                    Toast.LENGTH_LONG).show();
            return;
        }
        final String fileName = file.getName();
        if (attachmentAlreadyExists(fileName)) {
            Toast.makeText(mContext, R.string.attach_error_file_already_exists,
                    Toast.LENGTH_LONG).show();
            return;
        }
        mAttachmentContainer.addView(new AttachmentView(mContext, file, this));
        mAttachments.add(file);
    }

    private synchronized Boolean attachmentAlreadyExists(String name) {
        final Integer count = mAttachmentContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            final View thisView = mAttachmentContainer.getChildAt(i);
            if (!(thisView instanceof AttachmentView)) continue;
            if (((AttachmentView) thisView).getFile().getName().toLowerCase(Locale
                    .ENGLISH).contentEquals(name.toLowerCase(Locale.ENGLISH)))
                return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public void onAttachmentRemoved(AttachmentView removed) {
        mAttachmentContainer.removeView(removed);
        mAttachments.remove(removed.getFile());
    }
}
