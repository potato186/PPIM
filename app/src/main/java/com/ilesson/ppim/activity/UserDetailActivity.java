package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.UpdateInfo;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.RoundImageView;

import org.devio.takephoto.app.TakePhoto;
import org.devio.takephoto.model.InvokeParam;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.rong.eventbus.EventBus;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;

import static com.ilesson.ppim.activity.AvatarActivity.MODIFY_SUCCESS;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_user_info)
public class UserDetailActivity extends BaseActivity{
    @ViewInject(R.id.user_icon)
    public RoundImageView userIcon;
    @ViewInject(R.id.user_nike)
    private TextView userNike;
    @ViewInject(R.id.user_phone)
    private TextView userPhone;
    @ViewInject(R.id.user_sex)
    private TextView userSex;
    @ViewInject(R.id.user_birth)
    private TextView userBirth;
    private String iconPath;

    private TakePhoto takePhoto;
    private InvokeParam invokeParam;
    private boolean needFresh;
    private static final int MODIFY=56;
    public static final String INPUT_CANTONESE ="input_cantonese";
    public static final String TTS_CANTONESE ="tts_cantonese";
    public static final String SEX ="sex";
    public static final String BIRTH ="birth";
    private SimpleDateFormat formatter;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        famleType = SPUtils.get(SEX,false);
        setSexText();
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        String birth = SPUtils.get(BIRTH,"");
        if(TextUtils.isEmpty(birth)){
            birth = formatter.format(new Date());
        }
        userBirth.setText(birth);
        setUserInfo();
    }

    private void setSexText(){
        if(famleType){
            userSex.setText(R.string.famale);
        }else{
            userSex.setText(R.string.male);
        }
    }
    @Event(R.id.back)
    private void back(View view){
        exit();
    }

    @Event(value = R.id.icon_layout)
    private void icon_layout(View view) {
        needFresh = true;
        startActivityForResult(new Intent(this, AvatarActivity.class), MODIFY);
//        fromCamera(false);
    }

    @Event(value = R.id.nike_layout)
    private void nike_layout(View view) {
        needFresh = true;
        startActivityForResult(new Intent(this, ModifyNameActivity.class), MODIFY);
    }
    @Event(value = R.id.address_view_layout)
    private void toaddress(View view) {
        startActivity(new Intent(this, AddressListActivity.class));
    }
    @Event(value = R.id.select_language_layout)
    private void select_language_layout(View view) {
        showLanguageDialog();
    }

    @Event(value = R.id.sex_layout)
    private void sex_layout(View view) {
        showSexDialog();
    }
    @Event(value = R.id.birth_layout)
    private void birth_layout(View view) {
        showTimePicker();
    }
    @Event(value = R.id.code_layout)
    private void code_layout(View view) {
        startActivity(new Intent(this,UserCodeActivity.class));
    }

    private void setUserInfo() {
        String icon = SPUtils.get(LoginActivity.USER_ICON, "");
//        if (null == userIcon) {
//            return;
//        }
        if (!TextUtils.isEmpty(icon)) {
            DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
            builder.cacheInMemory(true).cacheOnDisk(true);
            ImageLoader.getInstance().displayImage(icon, userIcon,
                    builder.build());
        }
        String name = SPUtils.get(LoginActivity.USER_NAME, "");
        userNike.setText(name);
        String phone = SPUtils.get(LoginActivity.USER_PHONE, "");
        userPhone.setText(phone);
        needFresh = false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MODIFY_SUCCESS) {
            EventBus.getDefault().post(new UpdateInfo());
            setUserInfo();
            needFresh=true;
        }
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            exit();
        }
        return super.onKeyDown(keyCode, event);
    }
    private void exit(){
        if(needFresh){
//            setResult(MODIFY_SUCCESS,new Intent());
        }
        finish();
    }

    private ImageView mandrain1;
    private ImageView cantonese1;
    private ImageView mandrain2;
    private ImageView cantonese2;
    private void setTtsLanguage(boolean ttsCantonese){
        if(ttsCantonese){
            setSelectedState(cantonese2);
            setUnSelectedState(mandrain2);
        }else{
            setSelectedState(mandrain2);
            setUnSelectedState(cantonese2);
        }
    }
    private void setInputLanguage(boolean inputCantonese){
        if(inputCantonese){
            setSelectedState(cantonese1);
            setUnSelectedState(mandrain1);
        }else{
            setSelectedState(mandrain1);
            setUnSelectedState(cantonese1);
        }
    }
    private void setSex(boolean famleType){
        if(famleType){
            setUnSelectedState(male);
            setSelectedState(famale);
        }else{
            setSelectedState(male);
            setUnSelectedState(famale);
        }
    }
    private void setSelectedState(ImageView imageView){
        imageView.setImageResource(R.mipmap.checked);
    }
    private void setUnSelectedState(ImageView imageView){
        imageView.setImageResource(R.drawable.gray_edge_circle);
    }
    private boolean inputCantonese;
    private boolean ttsCantonese;
    private boolean famleType;
    private void showLanguageDialog(){
        View view = getLayoutInflater().inflate(R.layout.select_language_dialog,null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
        view.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        mandrain1 = view.findViewById(R.id.mandrain1);
        cantonese1 = view.findViewById(R.id.cantonese1);
        mandrain2 = view.findViewById(R.id.mandrain2);
        cantonese2 = view.findViewById(R.id.cantonese2);
        inputCantonese = SPUtils.get(INPUT_CANTONESE,false);
        ttsCantonese = SPUtils.get(TTS_CANTONESE,false);
        setInputLanguage(inputCantonese);
        setTtsLanguage(ttsCantonese);
        mandrain1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!inputCantonese){
                    return;
                }
                inputCantonese=false;
                setInputLanguage(false);
            }
        });
        cantonese1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputCantonese){
                    return;
                }
                inputCantonese=true;
                setInputLanguage(true);
            }
        });
        mandrain2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ttsCantonese){
                    return;
                }
                ttsCantonese=false;
                setTtsLanguage(false);
            }
        });
        cantonese2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ttsCantonese){
                    return;
                }
                ttsCantonese=true;
                setTtsLanguage(true);
            }
        });
        view.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtils.put(INPUT_CANTONESE,inputCantonese);
                SPUtils.put(TTS_CANTONESE,ttsCantonese);
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (PPScreenUtils.getScreenWidth(this) * 0.85);
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setAttributes(p);
        dialog.show();
    }

    private ImageView male;
    private ImageView famale;


    private void showSexDialog(){
        View view = getLayoutInflater().inflate(R.layout.select_sex_dialog,null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
        view.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        male = view.findViewById(R.id.male);
        famale = view.findViewById(R.id.famale);

        setSex(famleType);
        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!famleType){
                    return;
                }
                famleType=false;
                setSex(false);
            }
        });
        famale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(famleType){
                    return;
                }
                famleType=true;
                setSex(true);
            }
        });
        view.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtils.put(SEX,famleType);
                setSexText();
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (PPScreenUtils.getScreenWidth(this) * 0.85);
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setAttributes(p);
        dialog.show();
    }

    private void showTimePicker(){
       final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        Calendar startDate = Calendar.getInstance();//控件起始时间
        //注：（1）年份可以随便设置 (2)月份是从0开始的（0代表1月 11月代表12月），即设置0代表起始时间从1月开始
        //(3)日期必须从1开始，因为2月没有30天，设置其他日期时，2月份会从设置日期开始显示导致出现问题
        startDate.set(1900, 1, 1);//该控件从1900年1月1日开始
        //时间选择器
        TimePickerView pvTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
            public void onTimeSelect(final Date date, View v) {
                String choiceTime =  formatter.format(date);//日期 String
                userBirth.setText(choiceTime);
                long  startl = date.getTime();//日期 long
            }
        }).setDate(selectedDate)//设置系统时间为当前时间
                .setRangDate(startDate, selectedDate)//设置控件日期范围 也可以不设置默认1900年到2100年
//                .setType(new boolean[]{true, true, true})//设置年月日时分秒是否显示 true:显示 false:隐藏
//                .setLabel("年", "月", "日")
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
//                .setDividerColor(0xFF24AD9D)//设置分割线颜色
                .isCyclic(false)//是否循环显示日期 例如滑动到31日自动转到1日 有个问题：不能实现日期和月份联动
                .build();
        pvTime.show();
    }
}
