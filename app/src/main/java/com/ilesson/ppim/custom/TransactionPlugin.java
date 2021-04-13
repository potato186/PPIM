package com.ilesson.ppim.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.EditExchangeActivity;

import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.IPluginModule;

import static com.ilesson.ppim.activity.ChatInfoActivity.GROUP_ID;

/**
 * Created by potato on 2020/3/12.
 */

public class TransactionPlugin implements IPluginModule {
    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_ext_plugin_transaction_selector);
    }
    @Override
    public String obtainTitle(Context context) {
        return "换";
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
//        CustomizeMessage pokeMessage = CustomizeMessage.obtain("50");
//        Message message = Message.obtain(rongExtension.getTargetId(), Conversation.ConversationType.PRIVATE, pokeMessage);
//       boolean isPay = SPUtils.get(LOGIN_PAY, false);
//        if(!isPay){
//            fragment.getActivity().startActivity(new Intent(fragment.getActivity(), PayPwdActivity.class));
//        }else {
            Intent intent = new Intent(fragment.getActivity(), EditExchangeActivity.class);
            intent.putExtra(GROUP_ID,rongExtension.getTargetId());
            fragment.getActivity().startActivityForResult(intent,5);
//        }
//        new IMUtils().sendRedPack(rongExtension.getTargetId(),8,"一包烟");
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }
}
