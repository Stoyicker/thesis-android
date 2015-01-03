package org.thesis.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import org.thesis.android.CApplication;
import org.thesis.android.R;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.io.prefs.PreferenceAssistant;
import org.thesis.android.ui.adapter.NavigationDrawerAdapter;
import org.thesis.android.ui.util.BackPreImeAutoCompleteTextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import static org.thesis.android.io.prefs.PreferenceAssistant.PREF_USER_NAME;

public class NavigationDrawerFragment extends Fragment implements NavigationDrawerAdapter
        .INavigationDrawerCallback {
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private NavigationDrawerAdapter.INavigationDrawerCallback mCallbacks;
    private RecyclerView mDrawerList;
    private View mFragmentContainerView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private int mCurrentSelectedPosition;
    private ActionBarActivity mActivity;
    private Context mContext;
    private final Queue<Runnable> mWhenClosedTasks = new LinkedList<>();
    private BackPreImeAutoCompleteTextView mNameField;
    private ImageButton mEditNameButton;
    private Boolean mIsNameBeingEdited;
    private Boolean mNameEditSuccess;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mActionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected
                (item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, Boolean.FALSE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Required to not to transfer the click to the view behind.
            }
        });
        mDrawerList = (RecyclerView) view.findViewById(R.id.drawerList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mDrawerList.setLayoutManager(layoutManager);
        mDrawerList.setHasFixedSize(Boolean.TRUE);
        mDrawerList.setOverScrollMode(View.OVER_SCROLL_NEVER);

        mIsNameBeingEdited = Boolean.FALSE;
        mNameEditSuccess = Boolean.TRUE;
        mNameField = (BackPreImeAutoCompleteTextView) view.findViewById(R.id.name_field);
        mNameField.setThreshold(1);
        refreshAutoCompleteEntries(mNameField);

        mNameField.setText(
                PreferenceAssistant.readSharedString(mContext,
                        PreferenceAssistant.PREF_USER_NAME, ""));
        mNameField.clearFocus();
        mNameField.setOnBackPressedAdditionalTask(new Runnable() {
            @Override
            public void run() {
                requestNameEditCancel();
            }
        });
        mEditNameButton = (ImageButton) view.findViewById(R.id.edit_button);
        mEditNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNameField.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final Integer newDrawableId;
                        if (mIsNameBeingEdited) {
                            finishNameEdit(mNameEditSuccess);
                            mNameEditSuccess = Boolean.TRUE;
                            newDrawableId = R.drawable.ic_edit;
                        } else {
                            mNameField.setFocusable(Boolean.TRUE);
                            mNameField.setFocusableInTouchMode(Boolean.TRUE);
                            startNameEdit();
                            newDrawableId = R.drawable.ic_check;
                        }
                        mEditNameButton.setImageDrawable(NavigationDrawerFragment.this.mContext
                                .getResources().getDrawable(newDrawableId));
                    }
                }, 400);
            }
        });

        final List<NavigationDrawerAdapter.NavigationItem> navigationItems = readMenuItems();
        NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(mContext, navigationItems);
        adapter.setNavigationDrawerCallbacks(this);
        mDrawerList.setAdapter(adapter);
        selectItem(mCurrentSelectedPosition);
        return view;
    }

    private void refreshAutoCompleteEntries(AutoCompleteTextView autoCompleteTextView) {
        List<String> allNames = SQLiteDAO.getInstance().getNames();
        String[] entries = allNames.toArray(new String[allNames.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.select_dialog_item,
                entries);
        autoCompleteTextView.setAdapter(adapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = PreferenceAssistant.readSharedBoolean(mActivity,
                PreferenceAssistant.PREF_USER_LEARNED_DRAWER, Boolean.FALSE);
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = Boolean.TRUE;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerAdapter.INavigationDrawerCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
        mActivity = (ActionBarActivity) activity;
        mContext = CApplication.getInstance().getContext();
    }

    public void setup(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = mActivity.findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mActionBarDrawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public synchronized void onDrawerClosed(View drawerView) {
                if (mIsNameBeingEdited) {
                    mNameField.post(new Runnable() {
                        @Override
                        public void run() {
                            mNameEditSuccess = Boolean.FALSE;
                            mEditNameButton.performClick();
                        }
                    });
                }
                super.onDrawerClosed(drawerView);
            }

            @Override
            public synchronized void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) return;
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = Boolean.TRUE;
                    PreferenceAssistant.saveSharedBoolean(mActivity, PreferenceAssistant
                            .PREF_USER_LEARNED_DRAWER, Boolean.TRUE);
                }
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState)
            mDrawerLayout.openDrawer(mFragmentContainerView);

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mActionBarDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private synchronized void finishNameEdit(Boolean success) {
        mNameField.setFocusable(Boolean.FALSE);
        mNameField.setFocusableInTouchMode(Boolean.FALSE);
        mIsNameBeingEdited = Boolean.FALSE;
        final InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(
                        Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNameField.getWindowToken(), 0);
        mNameField.clearFocus();
        if (success) {
            final String name = mNameField.getText().toString();
            PreferenceAssistant.saveSharedString(mContext,
                    PreferenceAssistant.PREF_USER_NAME, name);
            SQLiteDAO.getInstance().addUserName(name);
            refreshAutoCompleteEntries(mNameField);
        }
        mNameField.setText(PreferenceAssistant.readSharedString(mContext, PREF_USER_NAME, ""));
    }

    private synchronized void startNameEdit() {
        mIsNameBeingEdited = Boolean.TRUE;
        mNameField.requestFocus();
        final InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(
                        Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mNameField, 0);
    }

    public void closeDrawer(Runnable... runnables) {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
        Collections.addAll(mWhenClosedTasks, runnables);
        while (!mWhenClosedTasks.isEmpty()) {
            mActivity.runOnUiThread(mWhenClosedTasks.poll());
        }
    }

    public List<NavigationDrawerAdapter.NavigationItem> readMenuItems() {
        List<NavigationDrawerAdapter.NavigationItem> items = new ArrayList<>();
        Resources resources = mContext.getResources();
        final List<String> tagGroups = SQLiteDAO.getInstance().getTagGroups();
        for (String x : tagGroups) {
            items.add(new NavigationDrawerAdapter.NavigationItem(x,
                    resources.getDrawable(R.drawable.ic_message_group)));
        }
        final String NAVIGATION_TITLE_STANDARD_DRAWABLE_PATTERN = mContext.getString(R.string
                .navigation_title_standard_resource_pattern);
        final String[] itemNames = resources.getStringArray(R.array.navigation_drawer_items);
        final List<Drawable> standardItemIcons = new LinkedList<>();
        for (int i = 0; i < itemNames.length; i++) {
            final String standardDrawableResourceName = String.format
                    (Locale.ENGLISH, NAVIGATION_TITLE_STANDARD_DRAWABLE_PATTERN, i);
            try {
                final Field standardDrawableResourceField = R.drawable.class.getDeclaredField
                        (standardDrawableResourceName);
                standardItemIcons.add(resources.getDrawable(standardDrawableResourceField.getInt
                        (standardDrawableResourceField)));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException("Expected drawable resource " +
                        standardDrawableResourceName + " not found.");
            }
        }
        if (standardItemIcons.size() < itemNames.length) {
            throw new IllegalStateException("Not enough icons for this many navigation choices");
        }
        for (int i = 0; i < itemNames.length; i++) {
            items.add(new NavigationDrawerAdapter.NavigationItem(itemNames[i],
                    standardItemIcons.get(i)));
        }
        return items;
    }

    public void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerLayout != null) {
            closeDrawer();
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
        ((NavigationDrawerAdapter) mDrawerList.getAdapter()).selectPosition(position);
    }

    public Integer getPosition() {
        return ((NavigationDrawerAdapter) mDrawerList.getAdapter()).getSelectedPosition();
    }

    public Boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        selectItem(position);
    }

    public Boolean requestNameEditCancel() {
        if (mIsNameBeingEdited) {
            mNameEditSuccess = Boolean.FALSE;
            mEditNameButton.performClick();
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
