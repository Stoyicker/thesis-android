package org.thesis.android.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import org.thesis.android.R;
import org.thesis.android.ui.adapter.NavigationDrawerAdapter;
import org.thesis.android.ui.fragment.NavigationDrawerFragment;

public class NavigationDrawerActivity extends ActionBarActivity implements
        NavigationDrawerAdapter.INavigationDrawerCallback {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private static final String USER_NAME = "USER_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_drawer_fragment);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(Boolean.TRUE);

        mNavigationDrawerFragment.setup(R.id.navigation_drawer_fragment,
                (DrawerLayout) findViewById(R.id.navigation_drawer), toolbar);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (mNavigationDrawerFragment == null || mNavigationDrawerFragment.getPosition() ==
                position) {
            return;
        }
    }

    @Override
    public void onBackPressed() {
        Boolean handled = Boolean.FALSE;

        if (mNavigationDrawerFragment.isDrawerOpen()) {
            if (!mNavigationDrawerFragment.requestNameEditCancel()) {
                mNavigationDrawerFragment.closeDrawer();
            }
            handled = Boolean.TRUE;
        }

        if (!handled) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            for (int i = fragmentManager.getBackStackEntryCount() - 1; i >= 0 && !handled; i--) {
                Fragment thisFragment = fragmentManager.findFragmentByTag(fragmentManager
                        .getBackStackEntryAt(i).getName());
                if (thisFragment instanceof IOnBackPressedListener)
                    handled = ((IOnBackPressedListener) thisFragment).onBackPressed();
            }
        }

        if (!handled)
            super.onBackPressed();
    }

    public interface IOnBackPressedListener {
        public Boolean onBackPressed();
    }
}
