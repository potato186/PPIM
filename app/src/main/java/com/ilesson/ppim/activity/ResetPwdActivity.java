package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
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
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.entity.SmsInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;

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
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_reset_pwd)
public class ResetPwdActivity extends BaseActivity{
    @ViewInject(R.id.phone_edit)
    private EditText phoneEdit;
    @ViewInject(R.id.code_edit)
    private EditText codeEdit;
    @ViewInject(R.id.pwd_edit)
    private EditText pwdEdit;
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.pwd_tips)
    private View pwdTips;
//    @ViewInject(R.id.pwd_edit1)
//    private EditText pwdEdit1;
    @ViewInject(R.id.get_code)
    private TextView mGetCodeTextView;
    @ViewInject(R.id.phone_code)
    private TextView mCodeView;
    @ViewInject(R.id.phone_layout)
    private View phoneLayout;
//    @ViewInject(R.id.pwd_layout1)
//    private View pwdLayout1;
    @ViewInject(R.id.pwd_layout2)
    private View pwdLayout2;
    private String countryCode = "86";
    private String countryName = "CN";
    private List<ContryCode> contryCodes;
    private static final String TAG = "ResetPwdActivity";
    public static final String RESET_LOGIN_PWD = "reset_login_pwd";
    public static final String RESET_PAY_PWD = "reset_pay_pwd";
    public static final String RESET_FROM_BOOT = "reset_from_boot";
    private boolean login;
    private boolean fromboot;
    private String mPhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        SMSSDK.registerEventHandler(eh);
        login = getIntent().getBooleanExtra(RESET_LOGIN_PWD,false);
        fromboot = getIntent().getBooleanExtra(RESET_FROM_BOOT,false);
        if(!login){
            pwdEdit.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD|InputType.TYPE_CLASS_NUMBER);
//            pwdEdit1.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD|InputType.TYPE_CLASS_NUMBER);
            pwdEdit.setHint(R.string.input_six_length_pwd);
            title.setText(R.string.reset_pay_pwd);
            pwdTips.setVisibility(View.GONE);
//            pwdEdit1.setHint(R.string.input_six_length_pwd_again);
        }
        phoneLayout.setVisibility(View.VISIBLE);
//        pwdLayout1.setVisibility(View.VISIBLE);
        pwdLayout2.setVisibility(View.VISIBLE);
        contryCodes = new ArrayList<>();
        getContry();
//        checkPermissions();
    }
    @Event(value = R.id.comfirm)
    private void comfirm(View view) throws DbException {
        if(login){
            request();
            return;
        }
        resetPayPwd();
    }
    @Event(value = R.id.back_btn)
    private void back_btn(View view) throws DbException {
        if(fromboot){
            startActivity(new Intent(this,LoginActivity.class));
        }
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
//        String userPhone = SPUtils.get(LoginActivity.USER_PHONE,"");
//        if(!TextUtils.isEmpty(userPhone)&&!userPhone.equals(mPhone)){
//            Toast.makeText(this,"请输入本次登录的手机号", Toast.LENGTH_SHORT).show();
//            return;
//        }
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
    private void getContry() {
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

    EventHandler eh = new EventHandler() {
        @Override
        public void afterEvent(int event, int result, Object data) {
            Log.d(TAG, "afterEvent: data=" + data);
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
                        mGetCodeTextView.setTextColor(getResources().getColor(R.color.grey_color));
                        time--;
                        handleros.sendEmptyMessageDelayed(TIME, delayed);
                    } else {
                        mGetCodeTextView.setText(R.string.get_code_again);
                        mGetCodeTextView.setTextColor(getResources().getColor(R.color.theme_color));
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
                    Toast.makeText(ResetPwdActivity.this,error, Toast.LENGTH_SHORT).show();
                    mGetCodeTextView.setEnabled(true);
                    break;
            }
        }
    };
    private void request() {
        String phone = phoneEdit.getText().toString();
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this,"手机号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String pwd = pwdEdit.getText().toString();
        if(TextUtils.isEmpty(pwd)){
            Toast.makeText(this,"新密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
//        String pwd1 = pwdEdit1.getText().toString();
//        if(!pwd1.equals(pwd)){
//            Toast.makeText(this,"两次输入的密码不一致", Toast.LENGTH_SHORT).show();
//            return;
//        }
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
        params.addBodyParameter("action", "reset_v3");
        params.addBodyParameter("country", countryName);
        params.addBodyParameter("mobile", phone);
        params.addBodyParameter("password", MD5.md5(pwd));
        params.addBodyParameter("otp", code);
        Log.d(TAG, "loadData: "+params.toString());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                removeDialog(DIALOG_PROGRESS);
                BaseCode<String> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<String>>() {
                        }.getType());
                hideProgress();
                if (base.getCode() == 0) {
                    SPUtils.put("bToken","");
                    Toast.makeText(ResetPwdActivity.this,"修改成功", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ResetPwdActivity.this,LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(ResetPwdActivity.this,base.getMessage(), Toast.LENGTH_SHORT).show();
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
    private void resetPayPwd() {
        String pwd = pwdEdit.getText().toString();
        if(pwd.length()!=6){
            Toast.makeText(this,R.string.input_six_length_pwd, Toast.LENGTH_SHORT).show();
            return;
        }
//        String pwd1 = pwdEdit.getText().toString();
//        if(!pwd1.equals(pwd)){
//            Toast.makeText(this,"两次输入的密码不一致", Toast.LENGTH_SHORT).show();
//            return;
//        }

        String code = codeEdit.getText().toString();
//        if(TextUtils.isEmpty(pwd)){
//            Toast.makeText(this,"密码不能为空", Toast.LENGTH_SHORT).show();
//            return;
//        }/pp/asset?action=update_pay_pwd&token=%s&pwd=%s&code=%s
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.ASSET_URL);
        params.addBodyParameter("action", "update_pay_pwd");
        params.addBodyParameter("token", SPUtils.get(LoginActivity.LOGIN_TOKEN,""));
        params.addBodyParameter("pwd", MD5.md5(pwd));
        params.addBodyParameter("code", code);
        Log.d(TAG, "loadData: "+params.toString());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                removeDialog(DIALOG_PROGRESS);
                BaseCode<PPUserInfo> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<PPUserInfo>>() {
                        }.getType());
                hideProgress();
                if (base.getCode() == 0) {
                    Toast.makeText(ResetPwdActivity.this,"修改成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ResetPwdActivity.this,base.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        if(fromboot){
            startActivity(new Intent(this,LoginActivity.class));
        }
        finish();
    }
}
