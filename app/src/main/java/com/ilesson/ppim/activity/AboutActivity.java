package com.ilesson.ppim.activity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.update.UpdateHelper;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_about)
public class AboutActivity extends BaseActivity{
    @ViewInject(R.id.version)
    private TextView mVisionTextView;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        String versionName = getAppVersionName(this);
        mVisionTextView.setText(versionName);
    }

    @Event(R.id.back_btn)
    private void back_btn(View view){
        finish();
    }
    @Event(R.id.new_version)
    private void new_version(View view){
        new UpdateHelper(this).checkVersion(true);
    }

    /**
     * 返回当前程序版本名1
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
