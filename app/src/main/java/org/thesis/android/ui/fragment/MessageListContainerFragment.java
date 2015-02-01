package org.thesis.android.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.thesis.android.CApplication;
import org.thesis.android.R;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.service.ManualSyncIntentService;
import org.thesis.android.ui.adapter.MessageListAdapter;
import org.thesis.android.ui.component.ChainableSwipeRefreshLayout;
import org.thesis.android.ui.component.EndlessRecyclerOnScrollListener;
import org.thesis.android.ui.component.tag.ITagCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;

public class MessageListContainerFragment extends Fragment implements ITagCard.ITagChangedListener {

    private static final String EXTRA_KEY_TAG_GROUP_TABLE = "EXTRA_KEY_TAG_GROUP_TABLE";
    private Context mContext;
    private SwipeRefreshLayout mRefreshLayout;
    private String mGroupName;
    public static final String EXTRA_KEY_TAG_LIST = "EXTRA_KEY_TAG_LIST";
    private MessageListAdapter mMessageListAdapter;
    private BroadcastReceiver mReceiver;

    public static Fragment newInstance(Context context, String tagGroup) {
        Bundle args = new Bundle();
        args.putString(EXTRA_KEY_TAG_GROUP_TABLE, tagGroup.toUpperCase(Locale.ENGLISH));

        return MessageListContainerFragment.instantiate(context, MessageListContainerFragment.class
                .getName(), args);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = CApplication.getInstance().getContext();
        final Bundle args;
        if ((args = getArguments()) != null)
            mGroupName = args.getString(EXTRA_KEY_TAG_GROUP_TABLE);
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance
                (mContext);
        localBroadcastManager.registerReceiver(mReceiver = new SyncDoneBroadcastReceiver(),
                new IntentFilter("org.thesis.android.SYNC_DONE"));
    }

    @Override
    public void onDetach() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_message_list_container, container,
                Boolean.FALSE);

        setUpRecyclerView(v);

        return v;
    }

    private void setUpRecyclerView(@NonNull View v) {
        final RecyclerView messageListRecyclerView = (RecyclerView) v.findViewById(R.id
                .message_recycler_view);
        messageListRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        final LinearLayoutManager layoutManager;
        messageListRecyclerView.setLayoutManager(layoutManager = new LinearLayoutManager(mContext));
        final View emptyView = v.findViewById(android.R.id.empty);
        messageListRecyclerView.setHasFixedSize(Boolean.FALSE);
        mMessageListAdapter =
                new MessageListAdapter(mContext, emptyView);
        messageListRecyclerView.setAdapter(mMessageListAdapter);
        messageListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        messageListRecyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(Integer currentPage) {
                mMessageListAdapter.showNextItemBatch();
            }
        });
        mRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refreshable_layout);
        ((ChainableSwipeRefreshLayout) mRefreshLayout).setRecyclerView(messageListRecyclerView);
        mRefreshLayout.setColorSchemeResources(R.color.material_deep_purple_900,
                R.color.material_deep_purple_900);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshLayout.setRefreshing(Boolean.TRUE);
                //Manually replicate a GCM communication
                final Intent intent = new Intent();
                final ArrayList<String> tagsAsArrayList = new ArrayList<>();
                //This hurts in my eyes
                tagsAsArrayList.addAll(SQLiteDAO.getInstance().getGroupTags
                        (mGroupName));
                intent.putStringArrayListExtra(EXTRA_KEY_TAG_LIST, tagsAsArrayList);

                ComponentName comp = new ComponentName(mContext.getPackageName(),
                        ManualSyncIntentService.class.getName());
                startWakefulService(mContext,
                        (intent.setComponent(comp)));
            }
        });
    }

    @Override
    public void onTagCreated(ITagCard tag) {
        //Do nothing
    }

    @Override
    public void onTagAdded(ITagCard tag) {
        //TODO Notify adapter
    }

    @Override
    public void onTagRemoved(ITagCard tag) {
        //TODO Notify adapter
    }

    @Override
    public void onTagCreationCancelled(ITagCard tag) {
        //Do nothing
    }

    private class SyncDoneBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final List<String> tagList;
            if (intent.getAction().contentEquals("org.thesis.android.SYNC_DONE") && (tagList = intent
                    .getStringArrayListExtra(EXTRA_KEY_TAG_LIST)) != null) {
                final List<String> l1 = new ArrayList<>(), l2 = new ArrayList<>();
                final List<String> currentTags = SQLiteDAO.getInstance().getGroupTags(mGroupName);
                for (String x : tagList) {
                    l1.add(x.toLowerCase(Locale.ENGLISH));
                }
                for (String x : currentTags) {
                    l2.add(x.toLowerCase(Locale.ENGLISH));
                }
                if (!Collections.disjoint(l1, l2))
                    mMessageListAdapter.requestDataLoad();
                mRefreshLayout.setRefreshing(Boolean.FALSE);
            }
        }
    }
}
