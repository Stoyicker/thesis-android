package org.thesis.android.ui.component;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class ChainableSwipeRefreshLayout extends SwipeRefreshLayout {

    private RecyclerView mRecyclerView;

    public ChainableSwipeRefreshLayout(Context context) {
        super(context);
    }

    public ChainableSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRecyclerView(RecyclerView _recyclerView) {
        mRecyclerView = _recyclerView;
    }

    @Override
    public boolean canChildScrollUp() {
        if (mRecyclerView == null)
            return super.canChildScrollUp();
        else
            return mRecyclerView.canScrollVertically(-1);
    }
}
