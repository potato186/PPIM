package com.ilesson.ppim.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.ConversationActivity;

import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;

import static com.mob.tools.utils.Strings.getString;

/**
 * Created by potato on 2020/3/12.
 */

public class CustomGroupPlugin implements IPluginModule {
    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_go_shop_group_selector);
    }
    @Override
    public String obtainTitle(Context context) {
        return "人工客服";
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
        ConversationActivity activity = (ConversationActivity) fragment.getActivity();
        RongIM.getInstance().startConversation(activity, Conversation.ConversationType.PRIVATE,"13823039350",activity.title+getString(R.string.custom_server));
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }
}
