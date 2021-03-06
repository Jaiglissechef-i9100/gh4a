package com.gh4a.activities;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;
import com.gh4a.Constants;
import com.gh4a.Gh4Application;
import com.gh4a.R;
import com.gh4a.db.BookmarksProvider;
import com.gh4a.fragment.PrivateEventListFragment;
import com.gh4a.fragment.PublicEventListFragment;
import com.gh4a.fragment.RepositoryIssueListFragment;
import com.gh4a.fragment.UserFragment;
import com.gh4a.utils.StringUtils;

public class UserActivity extends BaseSherlockFragmentActivity {

    public String mUserLogin;
    public String mUserName;
    private boolean mIsLoginUserPage;
    private ViewPager mPager;
    private UserFragment mUserFragment;
    private PrivateEventListFragment mPrivateEventListFragment;
    private PublicEventListFragment mPublicEventListFragment;
    private RepositoryIssueListFragment mRepositoryIssueListFragment;
    public boolean isFinishLoadingFollowing;
    public boolean isFollowing;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Gh4Application.THEME);
        super.onCreate(savedInstanceState);
        
        Bundle data = getIntent().getExtras();
        mUserLogin = data.getString(Constants.User.USER_LOGIN);
        mUserName = data.getString(Constants.User.USER_NAME);
        
        if (!isOnline()) {
            setErrorView();
            return;
        }

        setContentView(R.layout.view_pager);

        BugSenseHandler.setup(this, "6e1b031");
        
        mIsLoginUserPage = mUserLogin.equals(getAuthLogin());
        
        ActionBar actionBar = getSupportActionBar();
        if (mIsLoginUserPage) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            mPager = setupPager(new UserAdapter(getSupportFragmentManager()), new int[] {
                R.string.about, R.string.user_news_feed,
                R.string.user_your_actions, R.string.issues
             });
        }
        else {
            actionBar.setTitle(mUserLogin);
            actionBar.setDisplayHomeAsUpEnabled(true);
            mPager = setupPager(new UserAdapter(getSupportFragmentManager()), new int[] {
                R.string.about, R.string.user_public_activity
             });
        }
    }
    
    public class UserAdapter extends FragmentStatePagerAdapter {

        public UserAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mIsLoginUserPage ? 4 : 2;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            if (position == 1) {
                mPrivateEventListFragment = (PrivateEventListFragment)
                        PrivateEventListFragment.newInstance(mUserLogin, mIsLoginUserPage);
                return mPrivateEventListFragment;
            }
            else if (position == 2) {
                mPublicEventListFragment = (PublicEventListFragment)
                        PublicEventListFragment.newInstance(mUserLogin, false);
                return mPublicEventListFragment;
            }
            else if (position == 3) {
                Map<String, String> filterData = new HashMap<String, String>();
                filterData.put("filter", "subscribed");
                mRepositoryIssueListFragment = RepositoryIssueListFragment.newInstance(filterData);
                return mRepositoryIssueListFragment;
            }
            else {
                return UserFragment.newInstance(mUserLogin, mUserName);
            }
        }
        
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        if (Gh4Application.THEME != R.style.LightTheme) {
            menu.findItem(R.id.refresh).setIcon(R.drawable.navigation_refresh_dark);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isSelf = mUserLogin.equals(getAuthLogin());

        MenuItem logoutAction = menu.findItem(R.id.logout);
        logoutAction.setTitle(isAuthorized() ? R.string.logout : R.string.login);
        logoutAction.setVisible(isSelf || !isAuthorized());

        MenuItem followAction = menu.findItem(R.id.follow);
        followAction.setVisible(!isSelf && isAuthorized());
        if (followAction.isVisible()) {
            if (!isFinishLoadingFollowing) {
                followAction.setActionView(R.layout.ab_loading);
                followAction.expandActionView();
            }
            else if (isFollowing) {
                followAction.setTitle(R.string.user_unfollow_action);
            }
            else {
                followAction.setTitle(R.string.user_follow_action);
            }
        }

        menu.findItem(R.id.bookmarks).setVisible(isSelf);
        menu.findItem(R.id.share).setVisible(!isSelf);
        menu.findItem(R.id.bookmark).setVisible(!isSelf);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void navigateUp() {
        if (isAuthorized()) {
            Gh4Application.get(this).openUserInfoActivity(this,
                    getAuthLogin(), null, Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        else {
            Intent intent = new Intent().setClass(this, Github4AndroidActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP
                    |Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                item.setActionView(R.layout.ab_loading);
                item.expandActionView();
                int currentTab = mPager.getCurrentItem();
                if (currentTab == 0 && mUserFragment != null) {
                    mUserFragment.refresh();
                } 
                else if (currentTab == 1 && mPrivateEventListFragment != null) {
                    mPrivateEventListFragment.refresh();
                }
                else if (currentTab == 2 && mPublicEventListFragment != null) {
                    mPublicEventListFragment.refresh();
                }
                else {
                    if (mRepositoryIssueListFragment != null) {
                        mRepositoryIssueListFragment.refresh();
                    }
                }
                return true;
            case R.id.dark:
                Gh4Application.THEME = R.style.DefaultTheme;
                saveTheme(R.style.DefaultTheme);
                return true;
            case R.id.light:
                Gh4Application.THEME = R.style.LightTheme;
                saveTheme(R.style.LightTheme);
                return true;
            case R.id.lightDark:
                Gh4Application.THEME = R.style.LightDarkTheme;
                saveTheme(R.style.LightDarkTheme);
                return true;
            case R.id.pub_timeline:
                Intent intent = new Intent().setClass(this, ExploreActivity.class);
                intent.putExtra("exploreItem", 0);
                startActivity(intent);
                return true;
            case R.id.trend:
                intent = new Intent().setClass(this, ExploreActivity.class);
                intent.putExtra("exploreItem", 1);
                startActivity(intent);
                return true;
            case R.id.blog:
                intent = new Intent().setClass(this, ExploreActivity.class);
                intent.putExtra("exploreItem", 2);
                startActivity(intent);
                return true;
            case R.id.follow:
                item.setActionView(R.layout.ab_loading);
                item.expandActionView();
                mUserFragment.followUser(mUserLogin);
                return true;
            case R.id.share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                int subjectId = StringUtils.isBlank(mUserName)
                        ? R.string.share_user_subject_loginonly : R.string.share_user_subject;
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(subjectId, mUserLogin, mUserName));
                shareIntent.putExtra(Intent.EXTRA_TEXT,  "https://github.com/" + mUserLogin);
                shareIntent = Intent.createChooser(shareIntent, getString(R.string.share_title));
                startActivity(shareIntent);
                return true;
            case R.id.bookmark:
                Intent bookmarkIntent = new Intent(this, getClass());
                bookmarkIntent.putExtra(Constants.User.USER_LOGIN, mUserLogin);
                bookmarkIntent.putExtra(Constants.User.USER_NAME, mUserName);
                saveBookmark(mUserLogin, BookmarksProvider.Columns.TYPE_USER, bookmarkIntent, mUserName);
                return true;
        }
        boolean result = super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.logout) {
            finish();
        }
        return result;
    }
    
    @SuppressLint("NewApi")
    private void saveTheme(int theme) {
        SharedPreferences sharedPreferences = getSharedPreferences(
                Constants.PREF_NAME, MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putInt("THEME", theme);
        editor.commit();
        
        recreate();
    }
    
    @Override
    public void recreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            super.recreate();
        } else {
            final Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            startActivity(intent);
            overridePendingTransition(0, 0);

            finish();
            overridePendingTransition(0, 0);
        }
    }
    
    @SuppressLint("NewApi")
    public void updateFollowingAction(boolean isFollowing) {
        this.isFollowing = isFollowing;
        this.isFinishLoadingFollowing = true;
        invalidateOptionsMenu();
    }
}
