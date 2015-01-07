package org.thesis.android.ui.activity;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.thesis.android.CApplication;
import org.thesis.android.R;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.ui.adapter.NavigationDrawerAdapter;
import org.thesis.android.ui.card.tag.ITagCard;
import org.thesis.android.ui.card.tag.TagCloudCardExpand;
import org.thesis.android.ui.fragment.MessageContainerFragment;
import org.thesis.android.ui.fragment.NavigationDrawerFragment;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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

        toolbar.findViewById(R.id.action_compose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        launchMessageCompositionActivity(null);
                    }
                }, 200);
            }
        });

        showInitialFragment();
    }

    private void launchMessageCompositionActivity(String tag) {
        final Intent homeIntent = new Intent(mContext,
                MessageCompositionActivity.class);
        if (!TextUtils.isEmpty(tag))
            homeIntent.putExtra(MessageCompositionActivity.EXTRA_TAG, tag);
        startActivity(homeIntent);
        overridePendingTransition(R.anim.move_in_from_bottom, R.anim.move_out_to_bottom);
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
        if (mTagCloudCard != null && mTagCloudCard.isExpanded()) {
            mTagCloudCard.setOnCollapseAnimatorEndListener(new Card.OnCollapseAnimatorEndListener
                    () {
                @Override
                public void onCollapseEnd(Card card) {
                    goToEntry(position);
                }
            });
            mTagCloudCard.doToogleExpand();
        } else
            goToEntry(position);
    }

    private void goToEntry(final Integer position) {

        final Fragment target = configureMessageContainer(position);
        findViewById(android.R.id.content).post(new Runnable() {
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
        showNewGroupDialog();
    }

    @Override
    public void onNavigationTagGroupRemovalRequested(final Integer pos) {
        mNavigationDrawerFragment.closeDrawer(new Runnable() {
            @Override
            public void run() {
                final Integer r;
                if (SQLiteDAO.getInstance().removeGroup(SQLiteDAO.getInstance().getTagGroups().get
                        (pos))) {
                    r = R.string.group_deletion_successful;
                } else {
                    r = R.string.error_group_deletion_unknown;
                }
                Toast.makeText(mContext, r, Toast.LENGTH_SHORT).show();
            }
        }, new Runnable() {
            @Override
            public void run() {
                mNavigationDrawerFragment.requestAdapterRefresh(pos);
                fixTagGroupIndexStackAfterRemovalOfIndex(pos);
            }
        });
    }

    private void fixTagGroupIndexStackAfterRemovalOfIndex(Integer removedPos) {
        final List<Integer> elems = new LinkedList<>();
        while (!mTagGroupIndexStack.empty()) {
            final Integer thisElement = mTagGroupIndexStack.pop();
            if (removedPos < thisElement) {
                elems.add(thisElement);
            } else if (removedPos > thisElement)
                elems.add(thisElement - 1);
        }

        Collections.reverse(elems);

        for (Integer i : elems)
            mTagGroupIndexStack.push(i);
    }

    public NavigationDrawerActivity() {
        super();
    }

    private void showNewGroupDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.new_tag_group_dialog_title)
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        final EditText nameField = (EditText) (
                                (MaterialDialog) dialog)
                                .getCustomView()
                                .findViewById(R.id.name_field);
                        nameField.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        nameField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView v, int actionId,
                                                          KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null &&
                                        event
                                                .isShiftPressed() && (event
                                        .getAction() == KeyEvent
                                        .ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
                                    ((MaterialDialog) dialog).getActionButton(DialogAction
                                            .POSITIVE).performClick();
                                    return Boolean.TRUE;
                                }
                                return Boolean.FALSE;
                            }
                        });

                        final InputMethodManager imm = (InputMethodManager)
                                NavigationDrawerActivity.this.mContext.getSystemService(Service
                                        .INPUT_METHOD_SERVICE);
                        imm.showSoftInput(nameField, 0);
                    }
                })
                .customView(R.layout.dialog_new_tag_group, Boolean.TRUE)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .titleColor(R.color.material_purple_900)
                .positiveColorRes(R.color.material_purple_900)
                .negativeColorRes(R.color.material_purple_900)
                .backgroundColor(android.R.color.white)
                .autoDismiss(Boolean.FALSE)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(final MaterialDialog materialDialog) {
                        final EditText nameField = (EditText) materialDialog.getCustomView()
                                .findViewById(R.id.name_field);
                        final String groupName;
                        if (!SQLiteDAO.getInstance().isTagOrGroupNameValid(groupName = nameField
                                .getText()
                                .toString())) {
                            Toast.makeText(NavigationDrawerActivity.this.mContext,
                                    R.string.invalid_tag_group_name, Toast.LENGTH_LONG).show();
                            return;
                        }
                        SQLiteDAO.getInstance().addGroup(groupName);
                        final InputMethodManager imm = (InputMethodManager)
                                NavigationDrawerActivity.this.mContext.getSystemService(Service
                                        .INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromInputMethod(nameField.getWindowToken(), 0);
                        mNavigationDrawerFragment.closeDrawer();
                        //Workaround for RecyclerView bug (https://code.google
                        // .com/p/android/issues/detail?id=77232)
                        NavigationDrawerActivity.this.findViewById(android.R.id.content)
                                .postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mNavigationDrawerFragment.requestAdapterRefresh(
                                                ((CheckBox) materialDialog.getCustomView()
                                                        .findViewById(R.id.browse_immediately))
                                                        .isChecked());
                                    }
                                }, 100);
                        materialDialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog materialDialog) {
                        materialDialog.dismiss();
                    }
                })
                .show();
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
            if (!mTagGroupIndexStack.isEmpty())
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
        CardView cardView = (CardView) findViewById(R.id.card_tag_group_configuration);
        mTagCloudCard = new Card(mContext);
        final Boolean doINeedToAddTheHeader = mTagCloudCard.getCardHeader() == null;
        final CardHeader header = doINeedToAddTheHeader ? new CardHeader(mContext) :
                mTagCloudCard.getCardHeader();
        final String groupName = SQLiteDAO.getInstance().getTagGroups().get(groupIndex);
        header.setTitle(groupName);
        if (doINeedToAddTheHeader) {
            header.setButtonExpandVisible(Boolean.TRUE);
            mTagCloudCard.addCardHeader(header);
        }

        final Boolean doINeedToAddTheExpand = mTagCloudCard.getCardExpand() == null;
        CardExpand cardExpand = doINeedToAddTheExpand ? new TagCloudCardExpand(mContext, this,
                cardView.findViewById(R.id.card_content_expand_layout)) : mTagCloudCard
                .getCardExpand();
        ((TagCloudCardExpand) cardExpand).setTagGroupAndRefreshViews(groupName);
        if (doINeedToAddTheExpand)
            mTagCloudCard.addCardExpand(cardExpand);


        if (mTagCloudCard.getOnCollapseAnimatorStartListener() == null)
            mTagCloudCard.setOnCollapseAnimatorStartListener(new Card
                    .OnCollapseAnimatorStartListener
                    () {
                @Override
                public void onCollapseStart(Card card) {
                    final TagCloudCardExpand tagCloudCardExpand = (TagCloudCardExpand) mTagCloudCard
                            .getCardExpand();
                    tagCloudCardExpand.cancelEdits();
                }
            });

        if (cardView.getCard() == null)
            cardView.setCard(mTagCloudCard);
        else
            cardView.replaceCard(mTagCloudCard);
    }

    public interface IOnBackPressedListener {
        public Boolean onBackPressed();
    }
}
