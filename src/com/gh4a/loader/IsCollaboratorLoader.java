package com.gh4a.loader;

import java.io.IOException;

import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;

import android.content.Context;

import com.gh4a.Gh4Application;

public class IsCollaboratorLoader extends BaseLoader<Boolean> {

    private String mRepoOwner;
    private String mRepoName;
    
    public IsCollaboratorLoader(Context context, String repoOwner, String repoName) {
        super(context);
        mRepoOwner = repoOwner;
        mRepoName = repoName;
    }
    
    @Override
    public Boolean doLoadInBackground() throws IOException {
        Gh4Application app = (Gh4Application) getContext().getApplicationContext();
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(app.getAuthToken());
        CollaboratorService collabService = new CollaboratorService(client);
        return collabService.isCollaborator(new RepositoryId(mRepoOwner, mRepoName), app.getAuthLogin());
    }
}
