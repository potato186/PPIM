package com.ilesson.ppim.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ilesson.ppim.IlessonApp;
import com.ilesson.ppim.R;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.FontResizeView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import static com.ilesson.ppim.IlessonApp.FONT_INDEX;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_modify_font_size)
public class ModifyFontActivity extends BaseActivity{
    @ViewInject(R.id.save)
    private TextView saveBtn;
    @ViewInject(R.id.preview)
    private TextView preview;
    @ViewInject(R.id.font_resize_view)
    private FontResizeView fontResizeView ;
    private float scalaSize=1f;
    private float selectSize;
    private boolean changed;
    private static final String TAG = "ModifyFontActivity";
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        scalaSize = SPUtils.get(FONT_INDEX, 1.0f);
//        fontResizeView.setSliderGrade();
        fontResizeView.setOnFontChangeListener(new FontResizeView.OnFontChangeListener() {
            @Override
            public void onFontChange(float fontSize) {
                preview.setTextSize(fontSize);
                preview.setText(R.string.preview_text_font);
                selectSize = fontSize;
                Log.d(TAG, "onFontChange: "+selectSize/14);
                if(fontSize==scalaSize*14){
                    changed = false;
                    saveBtn.setBackgroundResource(R.drawable.background_gray_corner5);
                }else{
                    changed = true;
                    saveBtn.setBackgroundResource(R.drawable.theme_gray_corer5_btn_selector);
                }
            }
        });
    }

    @Event(R.id.save)
    private void save(View view){
        if(changed){
            showDialog();
        }else{
            finish();
        }
    }

    @Event(R.id.back_btn)
    private void back_btn(View view){
        finish();
    }

    private void showDialog(){
        View view = getLayoutInflater().inflate(R.layout.practice_dialog,null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view).create();
        TextView scoreTv =  view.findViewById(R.id.content);
        scoreTv.setText(R.string.reset_font_tips);
        TextView left_btn =  view.findViewById(R.id.left_btn);
        left_btn.setText(R.string.cancel);
        TextView right_btn =  view.findViewById(R.id.right_btn);
        right_btn.setText(R.string.confirm);
        left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtils.put(FONT_INDEX, selectSize/14);
                IlessonApp.restartApp(ModifyFontActivity.this);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
