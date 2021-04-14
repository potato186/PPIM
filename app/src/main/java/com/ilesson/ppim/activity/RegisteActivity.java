package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.ContryCode;
import com.ilesson.ppim.entity.SmsInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.TextUtil;

import org.xutils.common.Callback;
import org.xutils.common.util.MD5;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * Created by potato on 2020/3/5.
 */
@ContentView(R.layout.activity_registe)
public class RegisteActivity extends BaseActivity{

    @ViewInject(R.id.name_edit)
    private EditText nameEdit;
    @ViewInject(R.id.pinyin_edit)
    private EditText pinyinEdit;
    @ViewInject(R.id.nike_edit)
    private EditText nickEdit;
    @ViewInject(R.id.phone_edit)
    private EditText phoneEdit;
    @ViewInject(R.id.code_edit)
    private EditText codeEdit;
    @ViewInject(R.id.pwd_edit)
    private EditText pwdEdit;
    @ViewInject(R.id.get_code)
    private TextView mGetCodeTextView;
    @ViewInject(R.id.phone_code)
    private TextView mCodeView;
    private static final String TAG = "RegisteActivity";
    private String mPhone;
    private String countryCode = "86";
    private String countryName = "CN";
    private List<ContryCode> contryCodes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        SMSSDK.registerEventHandler(eh);
        getContry();
        nameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                pinyinEdit.setText(TextUtil.getPinyin(s.toString()));
                if(!TextUtils.isEmpty(s)){

                }
            }
        });
//        checkPermissions();
    }
    @Event(value = R.id.registe)
    private void registe(View view) throws DbException {
        registe();
    }
    @Event(value = R.id.close)
    private void back_btn(View view) throws DbException {
        finish();
    }

    @Event(value = R.id.get_code)
    private void get_code(View view) throws DbException {
        mPhone = phoneEdit.getText().toString();
        Log.d(TAG, "get_code: "+mPhone);
        if(TextUtils.isEmpty(mPhone)){
            Toast.makeText(this,"手机号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        requestCode();
    }
    @Event(value = R.id.phone_code_layout)
    private void phone_code_layout(View view) throws DbException {
        showCountryDialog();
    }

    private void requestCode(){
        SMSSDK.getVerificationCode(countryCode, mPhone);
        mGetCodeTextView.setEnabled(false);
    }
    public static boolean isMobileNO(String mobiles) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(mobiles).matches();
    }
    private void getContry() {
        contryCodes = new ArrayList<>();
        RequestParams params = new RequestParams(Constants.BASE_URL+"/country/country.json");
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                hideProgress();
                List<ContryCode> datas = new Gson().fromJson(
                        result,
                        new TypeToken<List<ContryCode>>() {
                        }.getType());
                contryCodes.clear();
                contryCodes.addAll(datas);
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
            }
        });
    }
    private Dialog mCountryDialog;
    private void showCountryDialog(){
        mCountryDialog = new Dialog(this);
        mCountryDialog.setCanceledOnTouchOutside(false);
        Window window = mCountryDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        View view = View.inflate(this, R.layout.country_layout, null);
        ListView listView = view.findViewById(R.id.country_list);
        listView.setAdapter(new CountryAdapter(contryCodes));
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        window.setBackgroundDrawableResource(android.R.color.transparent);
        mCountryDialog.setCanceledOnTouchOutside(true);
        mCountryDialog.show();
    }

    EventHandler eh = new EventHandler() {
        @Override
        public void afterEvent(int event, int result, Object data) {
            Log.d(TAG, "afterEvent: data=" + data);
            removeDialog(DIALOG_PROGRESS);
            if (result == SMSSDK.RESULT_COMPLETE) {
                handleros.sendEmptyMessage(TIME);
                Log.d(TAG, "afterEvent: 回调完成");
                //回调完成
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    Log.d(TAG, "afterEvent: 提交验证码成功");
                    handleros.sendEmptyMessage(TRY_SUCESS);
                    //提交验证码成功
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    Log.d(TAG, "afterEvent: 获取验证码成功");
                    //获取验证码成功
                } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                    Log.d(TAG, "afterEvent: 返回支持发送验证码的国家列表");
                    //返回支持发送验证码的国家列表
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {

                }
            } else {
                Throwable tb = (Throwable) data;
                try{
                    SmsInfo info = new Gson().fromJson(tb.getMessage(),
                            SmsInfo.class);
                    Message msg = new Message();
                    msg.what=TRY_FAIL;
                    msg.obj = info.getDetail();
                    handleros.sendMessage(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    };

    private static final int TIME = 0;
    private static final int TRY_SUCESS = 1;
    private static final int TRY_FAIL = 2;
    private int time = 60;
    private long delayed = 1000;
    Handler handleros = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TIME:
                    if (time > 0) {
                        mGetCodeTextView.setText(time+getResources().getString(R.string.get_code_again_time));
//                        mGetCodeTextView.setTextColor(getResources().getColor(R.color.gray_text333_color));
                        time--;
                        handleros.sendEmptyMessageDelayed(TIME, delayed);
                    } else {
                        mGetCodeTextView.setText(R.string.get_code_again);
//                        mGetCodeTextView.setTextColor(getResources().getColor(R.color.gray_text333_color));
                        mGetCodeTextView.setEnabled(true);
                        time=60;
                    }
                    break;
                case TRY_SUCESS:
                    mGetCodeTextView.setEnabled(true);
//                    login();
                    break;
                case TRY_FAIL:
                    String error = (String) msg.obj;
                    Toast.makeText(RegisteActivity.this,error, Toast.LENGTH_SHORT).show();
                    mGetCodeTextView.setEnabled(true);
                    break;
            }
        }
    };
    private void registe() {
        String userName = nameEdit.getText().toString();
        if(TextUtils.isEmpty(userName)){
            Toast.makeText(this,R.string.hint_input_name, Toast.LENGTH_SHORT).show();
            return;
        }
        String pinyin = pinyinEdit.getText().toString();
        if(TextUtils.isEmpty(pinyin)){
            Toast.makeText(this,R.string.hint_input_name_pinyin, Toast.LENGTH_SHORT).show();
            return;
        }
        String phone = phoneEdit.getText().toString();
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this,"手机号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String nick = nickEdit.getText().toString();
        if(TextUtils.isEmpty(nick)||nick.length()<2){
            Toast.makeText(this,R.string.nike_rule6, Toast.LENGTH_SHORT).show();
            return;
        }
        String pwd = pwdEdit.getText().toString();
        if(pwd.length()<6){
            Toast.makeText(this,R.string.pwd_rule6, Toast.LENGTH_SHORT).show();
            return;
        }
        String code = codeEdit.getText().toString();
        if(TextUtils.isEmpty(code)){
            Toast.makeText(this,R.string.empty_sms, Toast.LENGTH_SHORT).show();
            return;
        }
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.BUSER_URL);
        params.addParameter("action", "signup_v3");
        params.addParameter("country", countryName);
        params.addParameter("mobile", phone);
        params.addParameter("password", MD5.md5(pwd));
        params.addParameter("name", nick);
        params.addParameter("real_name", userName);
        params.addParameter("name_symbol", pinyin);
        params.addParameter("otp", code);
        Log.d(TAG, "loadData: "+params.toString());
        showProgress();
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                BaseCode<String> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<String>>() {
                        }.getType());
                hideProgress();
                if (base.getCode() == 0) {
                    Toast.makeText(RegisteActivity.this,"注册成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegisteActivity.this,base.getMessage(), Toast.LENGTH_SHORT).show();
                }

                Log.d(TAG, "onSuccess: +" + result);
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
    private class CountryAdapter extends BaseAdapter {

        private List<ContryCode> data;

        private CountryAdapter(List<ContryCode> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            final ContryCode content = data.get(i);
            ViewHolder holder = null;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.phone_country_item, null);
                holder = new ViewHolder();
                holder.countryTextView = view.findViewById(R.id.country_name);
                holder.codeTextView = view.findViewById(R.id.country_code);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.countryTextView.setText(content.getCn());
            holder.codeTextView.setText(content.getTel());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    countryCode = content.getTel();
                    countryName = content.getName();
                    mCodeView.setText(content.getCn()+"     "+ countryCode);
                    mCountryDialog.dismiss();
                }
            });
            return view;
        }
    }
    class ViewHolder{
        private TextView countryTextView;
        private TextView codeTextView;
    }
}
  