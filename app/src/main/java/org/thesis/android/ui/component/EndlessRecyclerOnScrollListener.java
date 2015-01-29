package org.thesis.android.ui.component;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

    private Integer previousTotal = 0; // The total number of items in the dataset after the last
    // load
    private Boolean loading = Boolean.TRUE; // True if we are still waiting for the last set of data
    // to
    // load.
    private Integer visibleThreshold = 5; // The minimum amount of items to have below your current
    // scroll position before loading more.
    private Integer firstVisibleItem, visibleItemCount, totalItemCount;

    private Integer current_page = 1;

    private LinearLayoutManager mLinearLayoutManager;

    public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = Boolean.FALSE;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached, do something
            current_page++;

            onLoadMore(current_page);

            loading = Boolean.TRUE;
        }
    }

    public abstract void onLoadMore(Integer currentPage);
}