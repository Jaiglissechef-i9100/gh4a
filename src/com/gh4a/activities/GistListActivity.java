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
package com.gh4a.activities;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.gh4a.Constants;
import com.gh4a.Gh4Application;
import com.gh4a.R;
import com.gh4a.adapter.GistAdapter;

public class GistListActivity extends BaseSherlockFragmentActivity implements OnItemClickListener {

    private String mUserLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Gh4Application.THEME);
        super.onCreate(savedInstanceState);

        mUserLogin = getIntent().getExtras().getString(Constants.User.USER_LOGIN);
        
        if (!isOnline()) {
            setErrorView();
            return;
        }
        
        setContentView(R.layout.generic_list);
        
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setTitle(getResources().getQuantityString(R.plurals.gist, 0));
        mActionBar.setSubtitle(mUserLogin);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        
        new LoadGistTask(this).execute(mUserLogin);
    }
    
    private static class LoadGistTask extends AsyncTask<String, Void, List<Gist>> {

        private WeakReference<GistListActivity> mTarget;
        private boolean mException;
        
        public LoadGistTask(GistListActivity activity) {
            mTarget = new WeakReference<GistListActivity>(activity);
        }

        @Override
        protected List<Gist> doInBackground(String... params) {
            if (mTarget.get() != null) {
                try {
                    GitHubClient client = new GitHubClient();
                    client.setOAuth2Token(mTarget.get().getAuthToken());
                    GistService gistService = new GistService(client);
                    return gistService.getGists(params[0]);                    
                }
                catch (IOException e) {
                    Log.e(Constants.LOG_TAG, e.getMessage(), e);
                    mException = true;
                    return null;
                }
            }
            else {
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(List<Gist> result) {
            if (mTarget.get() != null) {
                GistListActivity activity = mTarget.get();
                activity.hideLoading();
                if (mException) {
                    activity.showError();
                }
                else {
                    activity.fillData(result);
                }
            }
        }
    }
    
    private void fillData(List<Gist> gists) {
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setOnItemClickListener(this);

        GistAdapter adapter = new GistAdapter(this);
        if (gists != null && !gists.isEmpty()) {
            adapter.addAll(gists);
            listView.setAdapter(adapter);
        }
        else {
            Gh4Application.get(this).notFoundMessage(this,
                    getResources().getQuantityString(R.plurals.gist, 1));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Gist gist = (Gist) adapterView.getAdapter().getItem(position);
        Gh4Application.get(this).openGistActivity(this, mUserLogin, gist.getId(), 0);
    }
    
    @Override
    protected void navigateUp() {
        Gh4Application.get(this).openUserInfoActivity(this, mUserLogin, null, Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
