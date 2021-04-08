package com.ilesson.ppim.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.OutlineActivity;
import com.ilesson.ppim.activity.PayPwdActivity;
import com.ilesson.ppim.activity.PayScoreActivity;
import com.ilesson.ppim.utils.SPUtils;

import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.IPluginModule;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_PAY;
import static com.ilesson.ppim.activity.OutlineActivity.CONVERSATIONTYPE;
import static com.ilesson.ppim.activity.PayScoreActivity.TARGET_ID;

/**
 * Created by potato on 2020/3/12.
 */

public class ComposePlugin implements IPluginModule {
    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_ext_plugin_compose_selector);
    }
    @Override
    public String obtainTitle(Context context) {
        return context.getResources().getString(R.string.compose_title);
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
            Intent intent = new Intent(fragment.getActivity(), OutlineActivity.class);
            intent.putExtra(TARGET_ID,rongExtension.getTargetId());
            intent.putExtra(CONVERSATIONTYPE,rongExtension.getConversationType());
            fragment.getActivity().startActivityForResult(intent,5);
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }
}
