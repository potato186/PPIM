package com.ilesson.ppim.custom;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.ComposeActivity;
import com.ilesson.ppim.activity.MapLocationActivity;
import com.ilesson.ppim.activity.PayScoreActivity;
import com.ilesson.ppim.utils.SPUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;
import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.widget.provider.LocationPlugin;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_PAY;
import static com.ilesson.ppim.activity.OutlineActivity.CONVERSATIONTYPE;
import static com.ilesson.ppim.activity.PayScoreActivity.TARGET_ID;

/**
 * Created by potato on 2020/3/12.
 */

public class PPLoctionPlugin extends LocationPlugin {

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
        locationPermissions(fragment,rongExtension);
    }

    private void locationPermissions(final Fragment fragment,final RongExtension rongExtension) {
        RxPermissions rxPermissions = new RxPermissions(fragment);
        rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    toLocation(fragment,rongExtension);
                } else {
                }
            }
        });
    }
    private void toLocation(Fragment fragment,RongExtension rongExtension) {
        Intent intent = new Intent(fragment.getActivity(), MapLocationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TARGET_ID,rongExtension.getTargetId());
        intent.putExtra(CONVERSATIONTYPE,rongExtension.getConversationType());
        fragment.getActivity().startActivity(intent);
    }
}
