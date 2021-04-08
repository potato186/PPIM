package com.ilesson.ppim.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.ilesson.ppim.R;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_feedback)
public class FeedBackActivity extends BaseActivity{
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
    }

    @Event(R.id.back_btn)
    private void back_btn(View view){
        finish();
    }
    @Event(R.id.submit)
    private void submit(View view){
        showProgress();
        handler.sendEmptyMessageDelayed(1,1000);
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            showToast(R.string.thanks_for_your_feedback);
            hideProgress();
            finish();
        }
    };
}
