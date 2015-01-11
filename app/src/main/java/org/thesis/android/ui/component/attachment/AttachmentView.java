package org.thesis.android.ui.component.attachment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.thesis.android.R;

import java.io.File;

@SuppressLint("ViewConstructor") //They wouldn't be used anyway
public class AttachmentView extends CardView implements View.OnClickListener {

    private final IOnAttachmentRemovedListener mCallback;
    private final File mFile;

    public AttachmentView(Context context, File attachmentFile, IOnAttachmentRemovedListener
            _callback) {
        super(context);
        View v = ((LayoutInflater) context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_view_attachment_card, this,
                Boolean.TRUE);


        final TextView textView = (TextView) v.findViewById(R.id.attachment_name);
        textView.setText(attachmentFile.getName());
        mCallback = _callback;
        mFile = attachmentFile;


        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        v.setVisibility(View.GONE);
        if (mCallback != null)
            mCallback.onAttachmentRemoved(this);
    }

    public File getFile() {
        return mFile;
    }

    public interface IOnAttachmentRemovedListener {

        public void onAttachmentRemoved(AttachmentView removed);
    }
}
