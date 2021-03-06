/*
 * Copyright 2011 Azwan Adli Abdullah
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gh4a.fragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.gh4a.Constants;
import com.gh4a.Gh4Application;
import com.gh4a.R;
import com.gh4a.activities.BaseSherlockFragmentActivity;
import com.gh4a.activities.FollowerFollowingListActivity;
import com.gh4a.activities.GistListActivity;
import com.gh4a.activities.OrganizationMemberListActivity;
import com.gh4a.activities.RepositoryListActivity;
import com.gh4a.activities.UserActivity;
import com.gh4a.loader.FollowUserLoader;
import com.gh4a.loader.IsFollowingUserLoader;
import com.gh4a.loader.LoaderCallbacks;
import com.gh4a.loader.LoaderResult;
import com.gh4a.loader.OrganizationListLoader;
import com.gh4a.loader.RepositoryListLoader;
import com.gh4a.loader.UserLoader;
import com.gh4a.utils.GravatarUtils;
import com.gh4a.utils.StringUtils;

public class UserFragment extends BaseFragment implements  OnClickListener {

    private String mUserLogin;
    private String mUserName;
    private User mUser;
    private Boolean isFollowing;
    private int mFollowersCount;
    private List<Repository> mTopRepos;
    private List<User> mOrgs;

    private LoaderCallbacks<User> mUserCallback = new LoaderCallbacks<User>() {
        @Override
        public Loader<LoaderResult<User>> onCreateLoader(int id, Bundle args) {
            return new UserLoader(getSherlockActivity(), mUserLogin);
        }
        @Override
        public void onResultReady(LoaderResult<User> result) {
            if (!checkForError(result)) {
                hideLoading();
                mUser = (User) result.getData();
                fillData();
            }
        }
    };

    private LoaderCallbacks<List<Repository>> mRepoListCallback = new LoaderCallbacks<List<Repository>>() {
        @Override
        public Loader<LoaderResult<List<Repository>>> onCreateLoader(int id, Bundle args) {
            Map<String, String> filterData = new HashMap<String, String>();
            filterData.put("sort", "pushed");
            return new RepositoryListLoader(getSherlockActivity(), mUserLogin,
                    mUser.getType(), filterData, 5);
        }
        @Override
        public void onResultReady(LoaderResult<List<Repository>> result) {
            if (!checkForError(result)) {
                hideLoading(R.id.pb_top_repos, 0);
                mTopRepos = (List<Repository>) result.getData();
                fillTopRepos();
            }
        }
    };

    private LoaderCallbacks<List<User>> mOrganizationCallback = new LoaderCallbacks<List<User>>() {
        @Override
        public Loader<LoaderResult<List<User>>> onCreateLoader(int id, Bundle args) {
            return new OrganizationListLoader(getSherlockActivity(), mUserLogin);
        }
        @Override
        public void onResultReady(LoaderResult<List<User>> result) {
            mOrgs = result.getData();
            fillOrganizations();
        }
    };

    private LoaderCallbacks<Boolean> mIsFollowingCallback = new LoaderCallbacks<Boolean>() {
        @Override
        public Loader<LoaderResult<Boolean>> onCreateLoader(int id, Bundle args) {
            return new IsFollowingUserLoader(getSherlockActivity(), mUserLogin);
        }
        @Override
        public void onResultReady(LoaderResult<Boolean> result) {
            UserActivity userActivity = (UserActivity) getSherlockActivity();
            isFollowing = result.getData();
            userActivity.updateFollowingAction(isFollowing);
        }
    };

    private LoaderCallbacks<Boolean> mFollowCallback = new LoaderCallbacks<Boolean>() {
        @Override
        public Loader<LoaderResult<Boolean>> onCreateLoader(int id, Bundle args) {
            return new FollowUserLoader(getSherlockActivity(), mUserLogin, isFollowing);
        }
        @Override
        public void onResultReady(LoaderResult<Boolean> result) {
            UserActivity userActivity = (UserActivity) getSherlockActivity();
            isFollowing = result.getData();
            userActivity.updateFollowingAction(isFollowing);
            TextView tvFollowersCount = (TextView) getView().findViewById(R.id.tv_followers_count);
            if (isFollowing) {
                tvFollowersCount.setText(String.valueOf(++mFollowersCount));
            }
            else {
                tvFollowersCount.setText(String.valueOf(--mFollowersCount));
            }
        }
    };

    public static UserFragment newInstance(String login, String name) {
        UserFragment f = new UserFragment();

        Bundle args = new Bundle();
        args.putString(Constants.User.USER_LOGIN, login);
        args.putString(Constants.User.USER_NAME, name);
        f.setArguments(args);
        
        return f;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLogin = getArguments().getString(Constants.User.USER_LOGIN);
        mUserName = getArguments().getString(Constants.User.USER_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.user, container, false);
        return v;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        showLoading();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mUser == null || mTopRepos == null || mOrgs == null || isFollowing == null) {
            refresh();
        }
    }
    
    public void refresh() {
        if (getLoaderManager().getLoader(0) == null) {
            getLoaderManager().initLoader(0, null, mUserCallback);
            getLoaderManager().initLoader(3, null, mIsFollowingCallback);
            getLoaderManager().initLoader(4, null, mFollowCallback);
        }
        else {
            getLoaderManager().restartLoader(0, null, mUserCallback);
            getLoaderManager().restartLoader(3, null, mIsFollowingCallback);
            getLoaderManager().restartLoader(4, null, mFollowCallback);
        }
        
        getLoaderManager().getLoader(0).forceLoad();
        getLoaderManager().getLoader(3).forceLoad();
    }
    
    private void fillData() {
        UserActivity activity = (UserActivity) getSherlockActivity();
        activity.invalidateOptionsMenu();
        View v = getView();
        Gh4Application app = (Gh4Application) getSherlockActivity().getApplication();
        Typeface boldCondensed = app.boldCondensed;
        Typeface regular = app.regular;
        
        AQuery aq = new AQuery(getSherlockActivity());
        aq.id(R.id.iv_gravatar).image(GravatarUtils.getGravatarUrl(mUser.getGravatarId()), 
                true, false, 0, 0, aq.getCachedImage(R.drawable.default_avatar), AQuery.FADE_IN);

        TextView tvName = (TextView) v.findViewById(R.id.tv_name);
        tvName.setTypeface(boldCondensed);
        
        TextView tvCreated = (TextView) v.findViewById(R.id.tv_created_at);
        tvCreated.setTypeface(regular);
        
        TextView tvFollowersCount = (TextView) v.findViewById(R.id.tv_followers_count);
        mFollowersCount = mUser.getFollowers();
        tvFollowersCount.setTypeface(boldCondensed);
        tvFollowersCount.setText(String.valueOf(mFollowersCount));
        
        TableLayout tlOrgMembers = (TableLayout) v.findViewById(R.id.cell_org_members);
        tlOrgMembers.setBackgroundResource(R.drawable.abs__list_selector_holo_dark);
        
        TableLayout tlFollowers = (TableLayout) v.findViewById(R.id.cell_followers);
        tlFollowers.setBackgroundResource(R.drawable.abs__list_selector_holo_dark);
        
        if (Constants.User.USER_TYPE_USER.equals(mUser.getType())) {
            tlFollowers.setOnClickListener(this);
            
            TextView tvFollowers = (TextView) v.findViewById(R.id.tv_followers_label);
            tvFollowers.setText(R.string.user_followers);
            
            tlOrgMembers.setVisibility(View.GONE);
        }
        else {
            tlOrgMembers.setOnClickListener(this);
            TextView tvMemberCount = (TextView) v.findViewById(R.id.tv_members_count);
            tvMemberCount.setTypeface(boldCondensed);
            
            tlFollowers.setVisibility(View.GONE);
        }
        
        //hide following if organization
        TableLayout tlFollowing = (TableLayout) v.findViewById(R.id.cell_following);
        tlFollowing.setBackgroundResource(R.drawable.abs__list_selector_holo_dark);
        if (Constants.User.USER_TYPE_USER.equals(mUser.getType())) {
            
            TextView tvFollowingCount = (TextView) v.findViewById(R.id.tv_following_count);
            tvFollowingCount.setTypeface(boldCondensed);
            tvFollowingCount.setText(String.valueOf(mUser.getFollowing()));
            
            tlFollowing.setOnClickListener(this);
        }
        else {
            tlFollowing.setVisibility(View.GONE);
        }
        
        TableLayout tlRepos = (TableLayout) v.findViewById(R.id.cell_repos);
        tlRepos.setBackgroundResource(R.drawable.abs__list_selector_holo_dark);
        
        TextView tvReposCount = (TextView) v.findViewById(R.id.tv_repos_count);
        tvReposCount.setTypeface(boldCondensed);
        
        if (mUserLogin.equals(((BaseSherlockFragmentActivity) getSherlockActivity()).getAuthLogin())) {
            tvReposCount.setText(String.valueOf(mUser.getTotalPrivateRepos() + mUser.getPublicRepos()));    
        }
        else {
            tvReposCount.setText(String.valueOf(mUser.getPublicRepos()));
        }
        
        tlRepos.setOnClickListener(this);
        
        //hide gists repos if organization
        TableLayout tlGists = (TableLayout) v.findViewById(R.id.cell_gists);
        tlGists.setBackgroundResource(R.drawable.abs__list_selector_holo_dark);
        if (Constants.User.USER_TYPE_USER.equals(mUser.getType())) {
            
            TextView tvGistsCount = (TextView) v.findViewById(R.id.tv_gists_count);
            tvGistsCount.setTypeface(boldCondensed);
            tvGistsCount.setText(String.valueOf(mUser.getPublicGists()));
            
            tlGists.setOnClickListener(this);
        }
        else {
            tlGists.setVisibility(View.GONE);
        }
        
        tvName.setText(StringUtils.isBlank(mUser.getName()) ? mUser.getLogin() : mUser.getName());
        if (Constants.User.USER_TYPE_ORG.equals(mUser.getType())) {
            tvName.append(" (");
            tvName.append(Constants.User.USER_TYPE_ORG);
            tvName.append(")");
        }

        if (mUser.getCreatedAt() != null) {
            tvCreated.setText(getString(R.string.user_created_at,
                    DateFormat.getMediumDateFormat(getActivity()).format(mUser.getCreatedAt())));
            tvCreated.setVisibility(View.VISIBLE);
        } else {
            tvCreated.setVisibility(View.GONE);
        }

        //show email row if not blank
        TextView tvEmail = (TextView) v.findViewById(R.id.tv_email);
        tvEmail.setTypeface(regular);
        if (!StringUtils.isBlank(mUser.getEmail())) {
            tvEmail.setText(mUser.getEmail());
            tvEmail.setVisibility(View.VISIBLE);
        }
        else {
            tvEmail.setVisibility(View.GONE);
        }
        
        //show website if not blank
        TextView tvWebsite = (TextView) v.findViewById(R.id.tv_website);
        if (!StringUtils.isBlank(mUser.getBlog())) {
            tvWebsite.setText(mUser.getBlog());
            tvWebsite.setVisibility(View.VISIBLE);
        }
        else {
            tvWebsite.setVisibility(View.GONE);
        }
        tvWebsite.setTypeface(regular);
        
        //show company if not blank
        TextView tvCompany = (TextView) v.findViewById(R.id.tv_company);
        if (!StringUtils.isBlank(mUser.getCompany())) {
            tvCompany.setText(mUser.getCompany());
            tvCompany.setVisibility(View.VISIBLE);
        }
        else {
            tvCompany.setVisibility(View.GONE);
        }
        tvCompany.setTypeface(regular);
        
        //Show location if not blank
        TextView tvLocation = (TextView) v.findViewById(R.id.tv_location);
        if (!StringUtils.isBlank(mUser.getLocation())) {
            tvLocation.setText(mUser.getLocation());
            tvLocation.setVisibility(View.VISIBLE);
        }
        else {
            tvLocation.setVisibility(View.GONE);
        }
        tvLocation.setTypeface(regular);
        
        TextView tvPubRepo = (TextView) v.findViewById(R.id.tv_pub_repos_label);
        tvPubRepo.setTypeface(boldCondensed);
        tvPubRepo.setTextColor(getResources().getColor(R.color.highlight));
        
        TextView tvOrgs = (TextView) v.findViewById(R.id.tv_orgs);
        tvOrgs.setTypeface(boldCondensed);
        tvOrgs.setTextColor(getResources().getColor(R.color.highlight));
        
        getLoaderManager().initLoader(1, null, mRepoListCallback);
        getLoaderManager().initLoader(2, null, mOrganizationCallback);
        
        getLoaderManager().getLoader(1).forceLoad();
        getLoaderManager().getLoader(2).forceLoad();
    }

    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.cell_followers) {
            getFollowers(view);
        } else if (id == R.id.cell_following) {
            getFollowing(view);
        } else if (id == R.id.cell_repos) {
            getPublicRepos(view);
        } else if (id == R.id.cell_gists) {
            getGists(view);
        } else if (id == R.id.cell_org_members) {
            getOrgMembers(view);
        }
    }

    public void getPublicRepos(View view) {
        Intent intent = new Intent().setClass(this.getActivity(), RepositoryListActivity.class);
        intent.putExtra(Constants.User.USER_LOGIN, mUserLogin);
        intent.putExtra(Constants.User.USER_NAME, mUserName);
        intent.putExtra(Constants.User.USER_TYPE, mUser.getType());
        startActivity(intent);
    }

    public void getFollowers(View view) {
        if (Constants.User.USER_TYPE_ORG.equals(mUser.getType())) {
            Intent intent = new Intent().setClass(this.getActivity(), OrganizationMemberListActivity.class);
            intent.putExtra(Constants.Repository.REPO_OWNER, mUserLogin);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent().setClass(this.getActivity(), FollowerFollowingListActivity.class);
            intent.putExtra(Constants.User.USER_LOGIN, mUserLogin);
            intent.putExtra("FIND_FOLLOWERS", true);
            startActivity(intent);
        }
    }

    public void getFollowing(View view) {
        Intent intent = new Intent().setClass(this.getActivity(), FollowerFollowingListActivity.class);
        intent.putExtra(Constants.User.USER_LOGIN, mUserLogin);
        intent.putExtra("FIND_FOLLOWERS", false);
        startActivity(intent);
    }

    public void getOrgMembers(View view) {
        Intent intent = new Intent().setClass(this.getActivity(), OrganizationMemberListActivity.class);
        intent.putExtra(Constants.Repository.REPO_OWNER, mUserLogin);
        startActivity(intent);
    }
    
    public void getGists(View view) {
        Intent intent = new Intent().setClass(this.getActivity(), GistListActivity.class);
        intent.putExtra(Constants.User.USER_LOGIN, mUserLogin);
        startActivity(intent);
    }

    public void fillTopRepos() {
        Gh4Application app = (Gh4Application) getSherlockActivity().getApplication();
        Typeface boldCondensed = app.boldCondensed;
        
        View v = getView();
        LinearLayout ll = (LinearLayout) v.findViewById(R.id.ll_top_repos);
        ll.removeAllViews();
        
        Button btnMore = (Button) getView().findViewById(R.id.btn_repos);
        
        int i = 0;
        if (mTopRepos != null) {
            for (final Repository repository : mTopRepos) {
                View rowView = getLayoutInflater(null).inflate(R.layout.row_simple_3, null);
                rowView.setBackgroundResource(R.drawable.abs__list_selector_holo_dark);
                rowView.setPadding(0, 16, 0, 16);
                rowView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Gh4Application app = (Gh4Application) getSherlockActivity().getApplication();
                        app.openRepositoryInfoActivity(getSherlockActivity(), repository);
                    }
                });
                
                TextView tvTitle = (TextView) rowView.findViewById(R.id.tv_title);
                tvTitle.setTypeface(boldCondensed);
                
                TextView tvDesc = (TextView) rowView.findViewById(R.id.tv_desc);
                tvDesc.setSingleLine(true);
                
                TextView tvExtra = (TextView) rowView.findViewById(R.id.tv_extra);
                tvExtra.setTextAppearance(getSherlockActivity(), R.style.default_text_micro);
                
                tvTitle.setText(repository.getOwner().getLogin() + "/" + repository.getName());
                
                if (!StringUtils.isBlank(repository.getDescription())) {
                    tvDesc.setVisibility(View.VISIBLE);
                    tvDesc.setText(repository.getDescription());
                }
                else {
                    tvDesc.setVisibility(View.GONE);
                }

                String language = repository.getLanguage() != null
                        ? repository.getLanguage() : getString(R.string.unknown);
                String extraData = getString(R.string.repo_search_extradata, language,
                        StringUtils.toHumanReadbleFormat(repository.getSize()),
                        repository.getForks(), repository.getWatchers());
                tvExtra.setText(extraData);
                
                ll.addView(rowView);
                
                //looks like the API ignore the per_page for GET /orgs/:org/repos
                if (i == 4) {
                    break;
                }
                i++;
            }
            
            if (!mTopRepos.isEmpty()) {
                btnMore.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getPublicRepos(view);
                    }
                });
            }
        }
        if (mTopRepos == null || mTopRepos.isEmpty()) {
            btnMore.setVisibility(View.GONE);
            TextView noRepos = new TextView(getSherlockActivity());
            noRepos.setText(R.string.no_repos_found);
            ll.addView(noRepos);
        }
    }
    
    public void fillOrganizations() {
        Gh4Application app = (Gh4Application) getSherlockActivity().getApplication();
        Typeface boldCondensed = app.boldCondensed;
        
        View v = getView();
        LinearLayout llOrgs = (LinearLayout) v.findViewById(R.id.ll_orgs);
        
        LinearLayout llOrg = (LinearLayout) v.findViewById(R.id.ll_org);
        llOrg.removeAllViews();
        
        if (mOrgs != null && !mOrgs.isEmpty()) {
            llOrgs.setVisibility(View.VISIBLE);
            for (final User org : mOrgs) {
                View rowView = getLayoutInflater(null).inflate(R.layout.row_simple, null);
                rowView.setBackgroundResource(R.drawable.abs__list_selector_holo_dark);
                rowView.setPadding(0, 16, 0, 16);
                rowView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Gh4Application app = (Gh4Application) getSherlockActivity().getApplication();
                        app.openUserInfoActivity(getSherlockActivity(), org.getLogin(), null);
                    }
                });
                
                TextView tvTitle = (TextView) rowView.findViewById(R.id.tv_title);
                tvTitle.setTypeface(boldCondensed);
                tvTitle.setText(org.getLogin());
                
                llOrg.addView(rowView);
            }
        }
        else {
            llOrgs.setVisibility(View.GONE);
        }
    }
    
    public void followUser(String userLogin) {
        getLoaderManager().restartLoader(4, null, mFollowCallback);
        getLoaderManager().getLoader(4).forceLoad();
    }
    
    private boolean checkForError(LoaderResult<?> result) {
        if (((BaseSherlockFragmentActivity) getSherlockActivity()).isLoaderError(result)) {
            hideLoading(R.id.pb_top_repos, 0);
            hideLoading();
            return true;
        }
        return false;
    }
}