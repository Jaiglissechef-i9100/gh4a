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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.ActionBar;
import com.gh4a.Constants;
import com.gh4a.Gh4Application;
import com.gh4a.R;

public class WikiActivity extends BaseSherlockFragmentActivity {

    private String mUserLogin;
    private String mRepoName;
    private String mTitle;
    private String mContent;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Gh4Application.THEME);
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.web_viewer);
        
        mUserLogin = getIntent().getStringExtra(Constants.Repository.REPO_OWNER);
        mRepoName = getIntent().getStringExtra(Constants.Repository.REPO_NAME);
        mTitle = getIntent().getStringExtra(Constants.Blog.TITLE);
        mContent = getIntent().getStringExtra(Constants.Blog.CONTENT);

        hideLoading();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mTitle);
        actionBar.setSubtitle(mUserLogin + "/" + mRepoName);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        fillData();
    }
    
    private void fillData() {
        
        WebView webView = (WebView) findViewById(R.id.web_view);

        WebSettings s = webView.getSettings();
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        s.setUseWideViewPort(false);
        s.setAllowFileAccess(true);
        s.setBuiltInZoomControls(false);
        s.setLightTouchEnabled(true);
        s.setLoadsImagesAutomatically(true);
        s.setSupportZoom(true);
        s.setSupportMultipleWindows(true);
        s.setJavaScriptEnabled(true);

        webView.setWebViewClient(webViewClient);
        webView.loadDataWithBaseURL("https://github.com", mContent, "text/html", "utf-8", null);
    }

    /** The web view client. */
    private WebViewClient webViewClient = new WebViewClient() {

        @Override
        public void onPageFinished(WebView webView, String url) {
            hideLoading();
        }
        
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    };
    
    @Override
    protected void navigateUp() {
        Intent intent = new Intent().setClass(this, WikiListActivity.class);
        intent.putExtra(Constants.Repository.REPO_OWNER, mUserLogin);
        intent.putExtra(Constants.Repository.REPO_NAME, mRepoName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
