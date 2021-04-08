package com.ilesson.ppim.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.EditExchangeActivity;
import com.ilesson.ppim.activity.PayPwdActivity;
import com.ilesson.ppim.activity.PayRedPacketActivity;
import com.ilesson.ppim.activity.PayScoreActivity;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.SPUtils;

import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.IPluginModule;

import static com.ilesson.ppim.activity.ChatInfoActivity.GROUP_ID;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_PAY;
import static com.ilesson.ppim.activity.PayScoreActivity.TARGET_ID;

/**
 * Created by potato on 2020/3/12.
 */

public class RedPacketPlugin implements IPluginModule {
    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_ext_plugin_redpacket_selector);
    }
    @Override
    public String obtainTitle(Context context) {
        return context.getResources().getString(R.string.pp_redpacket);
    }
    public static final int REDPACKET=6;
    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
        boolean isPay = SPUtils.get(LOGIN_PAY, false);
        if(!isPay){
            fragment.getActivity().startActivity(new Intent(fragment.getActivity(), PayPwdActivity.class));
        }else {
            Intent intent = new Intent(fragment.getActivity(), PayRedPacketActivity.class);
            intent.putExtra(TARGET_ID,rongExtension.getTargetId());
            fragment.getActivity().startActivityForResult(intent,REDPACKET);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode==REDPACKET){

        }
    }
}
