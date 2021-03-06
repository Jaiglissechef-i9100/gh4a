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
package com.gh4a.adapter;

import org.eclipse.egit.github.core.User;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.gh4a.Gh4Application;
import com.gh4a.R;
import com.gh4a.utils.GravatarUtils;
import com.gh4a.utils.StringUtils;

public class UserAdapter extends RootAdapter<User> {

    private boolean mShowExtraData;
    private AQuery aq;
    
    public UserAdapter(Context context, boolean showExtraData) {
        super(context);
        mShowExtraData = showExtraData;
        aq = new AQuery(context);
    }
    
    @Override
    public View doGetView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder viewHolder = null;
        Gh4Application app = (Gh4Application) mContext.getApplicationContext();
        
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) LayoutInflater.from(mContext);
            v = vi.inflate(R.layout.row_gravatar_1, parent, false);

            
            Typeface boldCondensed = app.boldCondensed;
            Typeface regular = app.regular;
            Typeface italic = app.italic;
            
            viewHolder = new ViewHolder();
            viewHolder.ivGravatar = (ImageView) v.findViewById(R.id.iv_gravatar);
            
            viewHolder.tvTitle = (TextView) v.findViewById(R.id.tv_title);
            viewHolder.tvTitle.setTypeface(boldCondensed);
            
            viewHolder.tvDesc = (TextView) v.findViewById(R.id.tv_desc);
            if (viewHolder.tvDesc != null) {
                viewHolder.tvDesc.setTypeface(regular);
            }
            
            viewHolder.tvExtra = (TextView) v.findViewById(R.id.tv_extra);
            if (viewHolder.tvExtra != null) {
                viewHolder.tvExtra.setTypeface(italic);
            }
            
            v.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final User user = mObjects.get(position);

        if (user != null) {
            aq.recycle(convertView);
            if (viewHolder.ivGravatar != null) {
                if (!StringUtils.isBlank(user.getGravatarId())) {
                    aq.id(viewHolder.ivGravatar).image(GravatarUtils.getGravatarUrl(user.getGravatarId()), 
                            true, false, 0, 0, aq.getCachedImage(R.drawable.default_avatar), 0);
                }
                else if (!StringUtils.isBlank(user.getEmail())) {
                    aq.id(viewHolder.ivGravatar).image(GravatarUtils.getGravatarUrl(StringUtils.md5Hex(user.getEmail())), 
                            true, false, 0, 0, aq.getCachedImage(R.drawable.default_avatar), 0);
                }
                else if (!StringUtils.isBlank(user.getAvatarUrl())) { 
                    aq.id(viewHolder.ivGravatar).image(user.getAvatarUrl(), 
                            true, false, 0, 0, aq.getCachedImage(R.drawable.default_avatar), 0);
                }
                else {
                    aq.id(viewHolder.ivGravatar).image(R.drawable.default_avatar);
                }
                
                viewHolder.ivGravatar.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        /** Open user activity */
                        if (!StringUtils.isBlank(user.getLogin())) {
                            Gh4Application context = (Gh4Application) v.getContext()
                                    .getApplicationContext();
                            context.openUserInfoActivity(v.getContext(), user.getLogin(), user
                                    .getLogin());
                        }
                    }
                });
            }

            if (viewHolder.tvTitle != null) {
                viewHolder.tvTitle.setText(StringUtils.formatName(user.getLogin(), user.getName()));
            }
            
            if (viewHolder.tvDesc != null) {
                viewHolder.tvDesc.setText(StringUtils.formatName(user.getLogin(), user.getName()));
            }

            if (viewHolder.tvExtra != null) {
                viewHolder.tvExtra.setVisibility(mShowExtraData ? View.VISIBLE : View.GONE);
                if (mShowExtraData) {
                    String extraData = v.getResources().getString(R.string.user_extra_data,
                            user.getFollowers(), user.getPublicRepos());
                    viewHolder.tvExtra.setText(extraData);
                }
            }
        }
        return v;
    }

    private static class ViewHolder {
        public TextView tvTitle;
        public ImageView ivGravatar;
        public TextView tvDesc;
        public TextView tvExtra;
    }

}