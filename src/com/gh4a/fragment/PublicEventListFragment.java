package com.gh4a.fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.gh4a.Constants;

public class PublicEventListFragment extends EventListFragment {

    public static EventListFragment newInstance(String login, boolean isPrivate) {
        EventListFragment f = new PublicEventListFragment();

        Bundle args = new Bundle();
        args.putString(Constants.User.USER_LOGIN, login);
        args.putBoolean(Constants.Event.IS_PRIVATE, isPrivate);
        f.setArguments(args);
        
        return f;
    }

    @Override
    public int getMenuGroupId() {
        return 2;
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getGroupId() == 2) { 
            open(item);
            return true;
        }
        else {
            return false;
        }
    }
}
