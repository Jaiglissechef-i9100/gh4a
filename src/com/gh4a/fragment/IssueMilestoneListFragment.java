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

import java.util.List;

import org.eclipse.egit.github.core.Milestone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.gh4a.Constants;
import com.gh4a.R;
import com.gh4a.activities.BaseSherlockFragmentActivity;
import com.gh4a.activities.IssueMilestoneEditActivity;
import com.gh4a.adapter.MilestoneAdapter;
import com.gh4a.loader.LoaderCallbacks;
import com.gh4a.loader.LoaderResult;
import com.gh4a.loader.MilestoneListLoader;

public class IssueMilestoneListFragment extends BaseFragment implements OnItemClickListener {
    private String mRepoOwner;
    private String mRepoName;
    private String mState;
    private ListView mListView;
    private MilestoneAdapter mAdapter;

    private LoaderCallbacks<List<Milestone>> mMilestoneCallback =
            new LoaderCallbacks<List<Milestone>>() {
        @Override
        public Loader<LoaderResult<List<Milestone>>> onCreateLoader(int id, Bundle args) {
            return new MilestoneListLoader(getSherlockActivity(), mRepoOwner, mRepoName, mState);
        }
        @Override
        public void onResultReady(LoaderResult<List<Milestone>> result) {
            hideLoading();
            if (!((BaseSherlockFragmentActivity) getSherlockActivity()).isLoaderError(result)) {
                fillData(result.getData());
            }
        }
    };
    
    public static IssueMilestoneListFragment newInstance(String repoOwner, String repoName, String state) {
        IssueMilestoneListFragment f = new IssueMilestoneListFragment();

        Bundle args = new Bundle();
        args.putString(Constants.Repository.REPO_OWNER, repoOwner);
        args.putString(Constants.Repository.REPO_NAME, repoName);
        args.putString(Constants.Milestone.STATE, state);
        f.setArguments(args);
        
        return f;
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRepoOwner = getArguments().getString(Constants.Repository.REPO_OWNER);
        mRepoName = getArguments().getString(Constants.Repository.REPO_NAME);
        mState = getArguments().getString(Constants.Milestone.STATE);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.generic_list, container, false);
        mListView = (ListView) v.findViewById(R.id.list_view);
        return v;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mAdapter = new MilestoneAdapter(getSherlockActivity());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        
        getLoaderManager().initLoader(0, null, mMilestoneCallback);
        getLoaderManager().getLoader(0).forceLoad();
    }
    
    private void fillData(List<Milestone> milestones) {
        mAdapter.addAll(milestones);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        MilestoneAdapter adapter = (MilestoneAdapter) adapterView.getAdapter();
        Milestone milestone = (Milestone) adapter.getItem(position);
        
        Intent intent = new Intent().setClass(getSherlockActivity(), IssueMilestoneEditActivity.class);
        intent.putExtra(Constants.Repository.REPO_OWNER, mRepoOwner);
        intent.putExtra(Constants.Repository.REPO_NAME, mRepoName);
        intent.putExtra(Constants.Milestone.NUMBER, milestone.getNumber());
        startActivity(intent);
    }
}