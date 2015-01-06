package org.thesis.android.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import org.thesis.android.CApplication;
import org.thesis.android.R;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.ui.adapter.NavigationDrawerAdapter;
import org.thesis.android.ui.card.tag.ITagCard;
import org.thesis.android.ui.card.tag.TagCloudCardExpand;
import org.thesis.android.ui.fragment.MessageContainerFragment;
import org.thesis.android.ui.fragment.NavigationDrawerFragment;

import java.util.Stack;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardView;

public class NavigationDrawerActivity extends ActionBarActivity implements
        NavigationDrawerAdapter.INavigationDrawerCallback, ITagCard.ITagChangedListener {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Context mContext;
    private Stack<Integer> mTagGroupIndexStack;
    private Card mTagCloudCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        FragmentManager supportFragmentManager = getSupportFragmentManager();

        mNavigationDrawerFragment = (NavigationDrawerFragment) supportFragmentManager
                .findFragmentById(R.id.navigation_drawer_fragment);

        mContext = CApplication.getInstance().getContext();

        mNavigationDrawerFragment.setup(R.id.navigation_drawer_fragment,
                (DrawerLayout) findViewById(R.id.navigation_drawer), toolbar);

        mTagGroupIndexStack = new Stack<>();

        mTagGroupIndexStack.push(0);

        setTagGroupConfigHeader(mTagGroupIndexStack.peek());
        showInitialFragment();
    }

    private void showInitialFragment() {
        Fragment fragment = configureMessageContainer(0);

        getSupportFragmentManager().beginTransaction().
                replace(R.id.content_fragment_container, fragment)
                .commitAllowingStateLoss(); //This transaction must not go into the back stack
        //Otherwise it might happen that the message content is hidden but the app is still
        //running
    }

    private Fragment configureMessageContainer(Integer tagGroupIndex) {
        return MessageContainerFragment.newInstance(mContext,
                SQLiteDAO.getInstance().getTagGroups().get(tagGroupIndex));
    }

    @Override
    public void onNavigationTagGroupSelected(final int position) {
        if (mNavigationDrawerFragment == null || mNavigationDrawerFragment.getPosition() ==
                position) {
            return;
        }

        if (mTagCloudCard != null && mTagCloudCard.isExpanded())
            mTagCloudCard.doToogleExpand();

        final Fragment target = configureMessageContainer(position);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager().beginTransaction().replace(R.id
                                .content_fragment_container,
                        target).addToBackStack(null).commitAllowingStateLoss();
                mTagGroupIndexStack.push(position);
                setTagGroupConfigHeader(mTagGroupIndexStack.peek());
            }
        });
    }

    @Override
    public void onNewGroupCreationRequested() {
        //TODO onNewGroupCreationRequested
    }

    @Override
    public void onBackPressed() {
        Boolean consumed = Boolean.FALSE;

        if (mNavigationDrawerFragment.isDrawerOpen()) {
            if (!mNavigationDrawerFragment.requestNameEditCancel()) {
                mNavigationDrawerFragment.closeDrawer();
            }
            consumed = Boolean.TRUE;
        }

        if (!consumed && mTagCloudCard != null)
            consumed = ((IOnBackPressedListener) mTagCloudCard.getCardExpand()).onBackPressed();

        if (!consumed) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            for (int i = fragmentManager.getBackStackEntryCount() - 1; i >= 0 && !consumed; i--) {
                Fragment thisFragment = fragmentManager.findFragmentByTag(fragmentManager
                        .getBackStackEntryAt(i).getName());
                if (thisFragment instanceof IOnBackPressedListener)
                    consumed = ((IOnBackPressedListener) thisFragment).onBackPressed();
            }
        }

        if (!consumed) {
            mTagGroupIndexStack.pop();
            setTagGroupConfigHeader(mTagGroupIndexStack.peek());
            super.onBackPressed();
        }
    }

    @Override
    public void onTagCreated(ITagCard tag) {
        //Unused
    }

    @Override
    public void onTagAdded(ITagCard tag) {
        SQLiteDAO.getInstance().addTagToGroupAndRemoveFromUngrouped(tag.getName(),
                SQLiteDAO.getInstance().getTagGroups().get(mTagGroupIndexStack.peek()));
    }

    @Override
    public void onTagRemoved(ITagCard tag) {
        SQLiteDAO.getInstance().removeTagFromGroup(tag.getName(),
                SQLiteDAO.getInstance().getTagGroups().get(mTagGroupIndexStack.peek()));
    }

    @Override
    public void onTagCreationCancelled(ITagCard tag) {
        //Unused
    }

    private void setTagGroupConfigHeader(Integer groupIndex) {
        mTagCloudCard = new Card(mContext);
        final CardHeader header = new CardHeader(mContext);
        final String groupName = SQLiteDAO.getInstance().getTagGroups().get(groupIndex);
        header.setTitle(groupName);
        header.setButtonExpandVisible(Boolean.TRUE);
        mTagCloudCard.addCardHeader(header);

        final CardView cardView = (CardView) findViewById(R.id.card_tag_group_configuration);

        final CardExpand cardExpand = new TagCloudCardExpand(mContext, this, groupName,
                cardView.findViewById(R.id.card_content_expand_layout));
        mTagCloudCard.addCardExpand(cardExpand);

        cardView.setCard(mTagCloudCard);
    }

    public interface IOnBackPressedListener {
        public Boolean onBackPressed();
    }
}
