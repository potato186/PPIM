package com.ilesson.ppim.activity;

import static com.ilesson.ppim.IlessonApp.FONT_INDEX;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.SetFont;
import com.ilesson.ppim.view.FontSliderBar;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import io.rong.eventbus.EventBus;


/**
 * Created by zsj on 2017/9/11.
 * 字体设置展示
 */
@ContentView(R.layout.activity_textsizeshow)
public class TextSizeShowActivity extends BaseActivity {
    @ViewInject(R.id.fontSliderBar)
    FontSliderBar fontSliderBar;
    @ViewInject(R.id.tv_chatcontent1)
    TextView tvContent1;
    @ViewInject(R.id.tv_chatcontent)
    TextView tvContent2;
    @ViewInject(R.id.tv_chatcontent3)
    TextView tvContent3;
    @ViewInject(R.id.save)
    TextView saveBtn;
    @ViewInject(R.id.iv_userhead)
    ImageView ivUserhead;
    private float textsize1, textsize2, textsize3;
    private float textSizef;//缩放比例
    private int currentIndex;
    private boolean isClickable = true;
    public static final float FONT_SCALE = 0.12f;
    @Event(R.id.save)
    private void save(View view){
        SPUtils.put(FONT_INDEX,fontSliderBar.getCurrentIndex());
        //通知主页面重启
        EventBus.getDefault().post(new SetFont());
        finish();
    }
    @Event(R.id.back_btn)
    private void back(View view){
            finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        initData();
    }

    private void initData() {
        currentIndex = SPUtils.get(FONT_INDEX, 1);
        textSizef = 1 + currentIndex * FONT_SCALE;
        textsize1 = tvContent1.getTextSize() / textSizef;
        textsize2 = tvContent2.getTextSize() / textSizef;
        textsize3 = tvContent3.getTextSize() / textSizef;
        fontSliderBar.setTickCount(6).setTickHeight(PPScreenUtils.dip2px(TextSizeShowActivity.this, 15)).setBarColor(Color.GRAY)
                .setTextColor(Color.BLACK).setTextPadding(PPScreenUtils.dip2px(TextSizeShowActivity.this, 10)).setTextSize(PPScreenUtils.dip2px(TextSizeShowActivity.this, 14))
                .setThumbRadius(PPScreenUtils.dip2px(TextSizeShowActivity.this, 10)).setThumbColorNormal(Color.GRAY).setThumbColorPressed(Color.GRAY)
                .setOnSliderBarChangeListener(new FontSliderBar.OnSliderBarChangeListener() {
                    @Override
                    public void onIndexChanged(FontSliderBar rangeBar, int index) {
                        if(index>5){
                            return;
                        }
                        index = index - 1;
                        float textSizef = 1 + index * FONT_SCALE;
                        setTextSize(textSizef);
                        if (currentIndex != fontSliderBar.getCurrentIndex()) {
                            saveBtn.setBackgroundResource(R.drawable.theme_gray_corer5_btn_selector);
                            saveBtn.setEnabled(true);
                        }else{
                            saveBtn.setBackgroundResource(R.drawable.background_gray_corner5);
                            saveBtn.setEnabled(false);
                        }
                    }
                }).setThumbIndex(currentIndex).withAnimation(false).applay();
    }

    private void setTextSize(float textSize) {
        //改变当前页面的字体大小
        tvContent1.setTextSize(PPScreenUtils.px2sp(TextSizeShowActivity.this, textsize1 * textSize));
        tvContent2.setTextSize(PPScreenUtils.px2sp(TextSizeShowActivity.this, textsize2 * textSize));
        tvContent3.setTextSize(PPScreenUtils.px2sp(TextSizeShowActivity.this, textsize3 * textSize));
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (currentIndex != fontSliderBar.getCurrentIndex()) {
//                if (isClickable) {
//                    isClickable = false;
//                    refresh();
//                }
//            } else {
//                finish();
//            }
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    private void refresh() {
//        //存储标尺的下标
//
////        new Handler().postDelayed(new Runnable() {
////            @Override
////            public void run() {
//////                hideMyDialog();
////                finish();
////            }
////        }, 2000);
//    }


}
