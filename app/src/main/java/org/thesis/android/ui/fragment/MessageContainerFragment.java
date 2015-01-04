package org.thesis.android.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.thesis.android.R;

import java.util.Locale;

public class MessageContainerFragment extends Fragment {

    private static final String EXTRA_TAG_GROUP_TABLE = "EXTRA_TAG_GROUP_TABLE";

    public static Fragment newInstance(Context context, String tagGroup) {
        Bundle args = new Bundle();
        args.putString(EXTRA_TAG_GROUP_TABLE, tagGroup.toUpperCase(Locale.ENGLISH));

        return MessageContainerFragment.instantiate(context, MessageContainerFragment.class
                .getName(), args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_message_container, container,
                Boolean.FALSE);
    }
}
