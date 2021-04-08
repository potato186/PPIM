package com.ilesson.ppim.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.ConversationActivity;

import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.IPluginModule;

/**
 * Created by potato on 2020/3/12.
 */

public class CustomServerPlugin implements IPluginModule {
    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_go_shop_server_selector);
    }
    @Override
    public String obtainTitle(Context context) {
        return "人工客服";
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
        ConversationActivity activity = (ConversationActivity) fragment.getActivity();
        activity.getServer();
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }
}
