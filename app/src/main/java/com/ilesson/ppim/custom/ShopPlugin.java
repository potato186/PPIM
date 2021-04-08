package com.ilesson.ppim.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.ShopSearchActivity;

import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.IPluginModule;

import static com.ilesson.ppim.activity.ChatInfoActivity.GROUP_ID;

/**
 * Created by potato on 2020/3/12.
 */

public class ShopPlugin implements IPluginModule {
    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_go_shop_plugin_compose_selector);
    }
    @Override
    public String obtainTitle(Context context) {
        return "邦邦导购";
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
            Intent intent = new Intent(fragment.getActivity(), ShopSearchActivity.class);
            intent.putExtra(GROUP_ID,rongExtension.getTargetId());
            fragment.getActivity().startActivityForResult(intent,5);
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }
}
