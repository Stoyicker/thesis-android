package org.thesis.android.datamodel;

import android.support.annotation.NonNull;

public final class MessageWrapper {

    private final String sender, body;
    private final Boolean mHasAttachments;

    public MessageWrapper(@NonNull final String sender, @NonNull final String body, @NonNull final Boolean mHasAttachments) {
        this.sender = sender;
        this.body = body;
        this.mHasAttachments = mHasAttachments;
    }

    public String getSender() {
        return sender;
    }

    public String getBody() {
        return body;
    }

    public Boolean hasAttachments() {
        return mHasAttachments;
    }
}
