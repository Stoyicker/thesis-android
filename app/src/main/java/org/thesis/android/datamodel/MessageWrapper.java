package org.thesis.android.datamodel;

import android.support.annotation.NonNull;

import java.util.Collection;

public final class MessageWrapper {

    private final String sender, body;
    private final Boolean mHasAttachments;
    private final Collection<String> mTags;

    public MessageWrapper(@NonNull final String sender, @NonNull final String body,
                          @NonNull final Boolean hasAttachments,
                          @NonNull final Collection<String> tags) {
        this.sender = sender;
        this.body = body;
        this.mHasAttachments = hasAttachments;
        this.mTags = tags;
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

    public Collection<String> getTags() {
        return mTags;
    }
}
