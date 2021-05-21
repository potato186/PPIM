package com.ilesson.ppim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.SPUtils;

import static com.ilesson.ppim.activity.ResetPwdActivity.RESET_FROM_BOOT;
import static com.ilesson.ppim.activity.ResetPwdActivity.RESET_LOGIN_PWD;

/**
 * Created by potato on 2020/3/11.
 */

public class BootActivity extends BaseActivity {
    public static final String GUIDE_STATE="guide_state";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot);
        initStatusBar();
//        View logoView = findViewById(R.id.logo);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
//        int width = PPScreenUtils.getScreenWidth(this);
//        int height = (int) ((double)(width*902)/(double)1080);
//        layoutParams.width = width;
//        layoutParams.height = height;
//        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//        logoView.setLayoutParams(layoutParams);
        handler.sendEmptyMessageDelayed(1,2000);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(TextUtils.isEmpty(SPUtils.get(GUIDE_STATE,""))){
                startActivity(new Intent(BootActivity.this,GuideActivity.class));
                finish();
                return;
            }
            String token = SPUtils.get("token", "");
            String btoken = SPUtils.get("bToken", "");
            if (!TextUtils.isEmpty(token)&&!TextUtils.isEmpty(btoken)) {
                Intent intent = new Intent(BootActivity.this,ResetPwdActivity.class);
                intent.putExtra(RESET_LOGIN_PWD,true);
                intent.putExtra(RESET_FROM_BOOT,true);
                startActivity(intent);
                finish();
                return;
            }
            if (TextUtils.isEmpty(token)) {
                startActivity(new Intent(BootActivity.this, LoginActivity.class));
            } else {
                startActivity(new Intent(BootActivity.this,MainActivity.class));
            }
            finish();
        }
    };
}
