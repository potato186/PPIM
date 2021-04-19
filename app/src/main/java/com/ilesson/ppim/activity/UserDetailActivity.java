package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.gson.Gson;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.UpdateInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.RoundImageView;

import org.devio.takephoto.app.TakePhoto;
import org.devio.takephoto.model.InvokeParam;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.rong.eventbus.EventBus;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;

import static com.ilesson.ppim.activity.AvatarActivity.MODIFY_SUCCESS;
import static com.ilesson.ppim.activity.LoginActivity.NAME_SYMBL;
import static com.ilesson.ppim.activity.LoginActivity.REAL_NAME;
import static com.ilesson.ppim.activity.ModifyNameActivity.MODIFY_REAL_NAME;
import static com.ilesson.ppim.activity.ModifyNameActivity.MODIFY_SYMBL;
import static com.ilesson.ppim.activity.ModifyNameActivity.MODIFY_TYPE;


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
    @ViewInject(R.id.language)
    private TextView language;
    @ViewInject(R.id.user_real_name)
    private TextView realName;
    @ViewInject(R.id.user_symbl)
    private TextView userSymbl;
    private String iconPath;

    private TakePhoto takePhoto;
    private InvokeParam invokeParam;
    private boolean needFresh;
    private static final int MODIFY=56;
    public static final String INPUT_CANTONESE ="input_cantonese";
    public static final String TTS_CANTONESE ="tts_cantonese";
    public static final String SEX ="sex";
    public static final String BIRTH ="birth";
    public static final String MALE ="1";
    public static final String FAMALE ="2";
    private SimpleDateFormat formatter;
    private String birthday;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        famleType = SPUtils.get(SEX,"");
        setSexText();
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        birthday = SPUtils.get(BIRTH,"");
//        if(TextUtils.isEmpty(birthday)){
//            birthday = formatter.format(new Date());
//        }
        inputCantonese = SPUtils.get(INPUT_CANTONESE,false);
        if(inputCantonese){
            language.setText(R.string.cantonese);
        }else{
            language.setText(R.string.mandarin);
        }
        userBirth.setText(birthday);
        setUserInfo();
    }

    private void setSexText(){
        if(famleType.equals(FAMALE)){
            userSex.setText(R.string.famale);
        }else if(famleType.equals(MALE)){
            userSex.setText(R.string.male);
        }else{
            userSex.setText(R.string.unknown);
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
        startActivityForResult(new Intent(this, ModifyNameActivity.class), MODIFY);
    }
    @Event(value = R.id.real_name_layout)
    private void real_name_layout(View view) {
        startActivityForResult(new Intent(this, ModifyNameActivity.class).putExtra(MODIFY_TYPE, MODIFY_REAL_NAME), MODIFY_REAL_NAME);
    }
    @Event(value = R.id.symble_layout)
    private void symble_layout(View view) {
        startActivityForResult(new Intent(this, ModifyNameActivity.class).putExtra(MODIFY_TYPE, MODIFY_SYMBL), MODIFY_SYMBL);
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
        String realname = SPUtils.get(REAL_NAME, "");
        realName.setText(realname);
        String symbol = SPUtils.get(NAME_SYMBL, "");
        userSymbl.setText(symbol);
        needFresh = false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==MODIFY_REAL_NAME){
            realName.setText(SPUtils.get(REAL_NAME,""));
            userSymbl.setText(SPUtils.get(NAME_SYMBL,""));
            return;
        }
        if(requestCode==MODIFY_SYMBL){
            userSymbl.setText(SPUtils.get(NAME_SYMBL,""));
            return;
        }
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

    private TextView mandrain1;
    private TextView cantonese1;
//    private ImageView mandrain2;
//    private ImageView cantonese2;
//    private void setTtsLanguage(boolean ttsCantonese){
//        if(ttsCantonese){
//            setSelectedState(cantonese2);
//            setUnSelectedState(mandrain2);
//        }else{
//            setSelectedState(mandrain2);
//            setUnSelectedState(cantonese2);
//        }
//    }
    private void setInputLanguage(boolean inputCantonese){
        if(inputCantonese){
            setSelectedState(cantonese1);
            setUnSelectedState(mandrain1);
        }else{
            setSelectedState(mandrain1);
            setUnSelectedState(cantonese1);
        }
    }
    private void setSex(String famleType){
        if(famleType.equals(FAMALE)){
            setUnSelectedState(male);
            setSelectedState(famale);
        }else{
            setSelectedState(male);
            setUnSelectedState(famale);
        }
    }
    private void setSelectedState(TextView textView){
        textView.setBackgroundResource(R.drawable.general_red_theme_corner20_selector);
        textView.setTextColor(getResources().getColor(R.color.white));
    }
    private void setUnSelectedState(TextView textView){
        textView.setBackgroundResource(R.drawable.background_gray_corner20);
        textView.setTextColor(getResources().getColor(R.color.second_blk_text));
    }
    private boolean inputCantonese;
//    private boolean ttsCantonese;
    private String famleType;
    private void showLanguageDialog(){
        View view = getLayoutInflater().inflate(R.layout.picker_item_dialog,null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
        view.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        mandrain1 = view.findViewById(R.id.option1);
        cantonese1 = view.findViewById(R.id.option2);
        cantonese1.setVisibility(View.GONE);
        TextView titleView = view.findViewById(R.id.textView);
        titleView.setText(R.string.select_language);
        mandrain1.setText(R.string.mandarin);
        cantonese1.setText(R.string.cantonese);
//        mandrain2 = view.findViewById(R.id.mandrain2);
//        cantonese2 = view.findViewById(R.id.cantonese2);
        inputCantonese = SPUtils.get(INPUT_CANTONESE,false);
//        ttsCantonese = SPUtils.get(TTS_CANTONESE,false);
        setInputLanguage(inputCantonese);
//        setTtsLanguage(ttsCantonese);
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
//        mandrain2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(!ttsCantonese){
//                    return;
//                }
//                ttsCantonese=false;
//                setTtsLanguage(false);
//            }
//        });
//        cantonese2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(ttsCantonese){
//                    return;
//                }
//                ttsCantonese=true;
//                setTtsLanguage(true);
//            }
//        });
        view.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtils.put(INPUT_CANTONESE,inputCantonese);
//                SPUtils.put(TTS_CANTONESE,ttsCantonese);
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (PPScreenUtils.getScreenWidth(this));
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        p.gravity = Gravity.BOTTOM;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setAttributes(p);
        dialog.show();
    }

    private TextView male;
    private TextView famale;


    private void showSexDialog(){
        View view = getLayoutInflater().inflate(R.layout.picker_item_dialog,null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
        view.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        male = view.findViewById(R.id.option1);
        famale = view.findViewById(R.id.option2);
        TextView titleView = view.findViewById(R.id.textView);
        titleView.setText(R.string.select_sex);
        if(famleType.equals("0")){
            famleType=MALE;
        }
        setSex(famleType);
        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!famleType.equals(FAMALE)){
                    return;
                }
                famleType=MALE;
                setSex(famleType);
            }
        });
        famale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(famleType.equals(FAMALE)){
                    return;
                }
                famleType=FAMALE;
                setSex(famleType);
            }
        });
        view.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifySex();
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (PPScreenUtils.getScreenWidth(this));
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        p.gravity = Gravity.BOTTOM;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setAttributes(p);
        dialog.show();
    }

    private void showTimePicker(){
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        Calendar startDate = Calendar.getInstance();//控件起始时间
        //注：（1）年份可以随便设置 (2)月份是从0开始的（0代表1月 11月代表12月），即设置0代表起始时间从1月开始
        //(3)日期必须从1开始，因为2月没有30天，设置其他日期时，2月份会从设置日期开始显示导致出现问题
        startDate.set(1900, 1, 1);//该控件从1900年1月1日开始
        //时间选择器
        TimePickerView pvTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
            public void onTimeSelect(final Date date, View v) {
                birthday =  formatter.format(date);//日期 String
                userBirth.setText(birthday);
                modifyBirthday();
                long  startl = date.getTime();//日期 long
            }
        }).setDate(selectedDate)//设置系统时间为当前时间
                .setRangDate(startDate, selectedDate)//设置控件日期范围 也可以不设置默认1900年到2100年
//                .setType(new boolean[]{true, true, true})//设置年月日时分秒是否显示 true:显示 false:隐藏
//                .setLabel("年", "月", "日")
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
//                .setDividerColor(0xFF24AD9D)//设置分割线颜色
                .isCyclic(false)//是否循环显示日期 例如滑动到31日自动转到1日 有个问题：不能实现日期和月份联动
                .setTitleText(getResources().getString(R.string.select_birthday))
                .setCancelColor(getResources().getColor(R.color.helptext_color))
                .setSubmitColor(getResources().getColor(R.color.second_blk_text))
                .build();
        pvTime.show();
    }

    private void modifySex() {
        //list&token=%s&page=%s&size=%s
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addBodyParameter("action", "mod_sex");
        params.addBodyParameter("sex", famleType);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "ExchangeActivity onSuccess: " + result);
                BaseCode base = new Gson().fromJson(result,BaseCode.class);
                if(base.getCode()==0){
                    SPUtils.put(SEX,famleType);
                    setSexText();
                }else{
                    showToast(base.getMessage());
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }


            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
                hideProgress();
            }
        });
    }
    private void modifyBirthday() {
        //list&token=%s&page=%s&size=%s
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addBodyParameter("action", "mod_birthday");
        params.addBodyParameter("birthday", birthday);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "ExchangeActivity onSuccess: " + result);
                BaseCode base = new Gson().fromJson(result,BaseCode.class);
                if(base.getCode()==0){
                    SPUtils.put(BIRTH,birthday);
                }else{
                    showToast(base.getMessage());
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }


            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
                hideProgress();
            }
        });
    }
    private void modifyR() {
        //list&token=%s&page=%s&size=%s
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addBodyParameter("action", "mod_birthday");
        params.addBodyParameter("birthday", birthday);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "ExchangeActivity onSuccess: " + result);
                BaseCode base = new Gson().fromJson(result,BaseCode.class);
                if(base.getCode()==0){
                    SPUtils.put(BIRTH,birthday);
                }else{
                    showToast(base.getMessage());
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }


            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
                hideProgress();
            }
        });
    }

    private static final String TAG = "UserDetailActivity";
    
}
