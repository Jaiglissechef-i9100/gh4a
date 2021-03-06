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
import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.OrganizationService;

import android.content.Intent;

import com.gh4a.Constants;
import com.gh4a.Gh4Application;
import com.gh4a.R;

public class OrganizationMemberListActivity extends UserListActivity {

    protected String mUserLogin;

    @Override
    protected void setRequestData() {
        mUserLogin = getIntent().getExtras().getString(Constants.Repository.REPO_OWNER);
    }

    @Override
    protected String getTitleBar() {
        return getResources().getString(R.string.members);
    }

    @Override
    protected String getSubTitle() {
        return mUserLogin;
    }

    @Override
    protected boolean getShowExtraData() {
        return false;
    }

    protected List<User> getUsers() throws IOException {
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(getAuthToken());
        OrganizationService orgService = new OrganizationService(client);
        return orgService.getPublicMembers(mUserLogin);
    }
    
    @Override
    protected void navigateUp() {
        Gh4Application.get(this).openUserInfoActivity(this, mUserLogin, null, Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}