package com.ilesson.ppim.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;

import static com.ilesson.ppim.activity.FriendDetailActivity.USER_INFO;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;

/**
 * Created by potato on 2020/3/11.
 */
@ContentView(R.layout.activity_pay_result)
public class PayResultActivity extends BaseActivity {
    @ViewInject(R.id.user_icon)
    private ImageView imageView;
    @ViewInject(R.id.user_name)
    private TextView userName;
    @ViewInject(R.id.money)
    private TextView moneyView;

    @Event(R.id.back_btn)
    private void back(View v){
        finish();
    }
    @Event(R.id.done)
    private void done(View v){
        finish();
    }
    private static final String TAG = "ContactActivity";
    public static final String PAY_MONEY = "pay_money";
    public static final String PAY_USER_NAME = "pay_user_name";
    public static final String PAY_USER_ICON = "pay_user_icon";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        Intent intent = getIntent();
        String name = intent.getStringExtra(PAY_USER_NAME);
        String icon = intent.getStringExtra(PAY_USER_ICON);
        String money = getIntent().getStringExtra(PAY_MONEY);
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
                builder.cacheInMemory(true).cacheOnDisk(true);
                ImageLoader.getInstance().displayImage(icon, imageView,
                        builder.build());
        userName.setText(name);
        moneyView.setText(money);
    }

}
