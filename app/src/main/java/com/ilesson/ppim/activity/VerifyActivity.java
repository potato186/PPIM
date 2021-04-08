package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.ilesson.ppim.custom.TransferMessage;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.BitLoginInfo;
import com.ilesson.ppim.entity.Close;
import com.ilesson.ppim.entity.ContryCode;
import com.ilesson.ppim.service.MyHttpManager;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.PwdCheckUtil;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

import io.rong.eventbus.EventBus;

import static com.ilesson.ppim.activity.InputPhoneActivity.TYPE_VERIFY;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_PAY;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_ICON;
import static com.ilesson.ppim.activity.LoginActivity.USER_MONEY;
import static com.ilesson.ppim.activity.LoginActivity.USER_NAME;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;
import static com.ilesson.ppim.activity.MainActivity.FRESH;
import static com.ilesson.ppim.fragment.FundFragment.ACTION_PAY;

/**
 * Created by potato on 2020/3/5.
 */
@ContentView(R.layout.act_reset_pwd)
public class VerifyActivity extends BaseActivity {

    @ViewInject(R.id.phone_edit)
    private EditText phoneEdit;
    @ViewInject(R.id.code_edit)
    private EditText codeEdit;
    @ViewInject(R.id.get_code)
    private TextView mGetCodeTextView;
    @ViewInject(R.id.phone_code)
    private TextView mCodeView;
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.phone_code_layout)
    private View phoneCodeLayout;
    @ViewInject(R.id.phone_layout)
    private View phoneLayout;
//    @ViewInject(R.id.pwd_layout1)
//    private View pwdLayout1;
    @ViewInject(R.id.pwd_layout2)
    private View pwdLayout2;
    @ViewInject(R.id.pwd_edit)
    private EditText pwdEdit;
    @ViewInject(R.id.pwd_edit1)
    private EditText pwdEdit1;
    private List<ContryCode> contryCodes;
    private String countryName = "CN";
    private String mCountryCode = "86";
    private static final String TAG = "VerifyActivity";
    public static final String VERIFY_ACTION = "verify_action";
    public static final String TEMP_TOKEN = "temp_token";
    public static final String PHONE = "phone";
    public static int VERIFY_TYPE = 0;
    public static final int REGISTE_TYPE = 10;
    public static final int FORGET_PWD_TYPE = 11;
    public static final int LOGIN_TYPE = 12;
    private String mPhone;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
        Intent intent = getIntent();
        boolean verify = intent.getBooleanExtra(TYPE_VERIFY,false);
        VERIFY_TYPE = intent.getIntExtra(VERIFY_ACTION, 0);
        token = intent.getStringExtra(TEMP_TOKEN);
        contryCodes = new ArrayList<>();
        if (VERIFY_TYPE == FORGET_PWD_TYPE) {
//            phoneCodeLayout.setVisibility(View.VISIBLE);
//            phoneLayout.setVisibility(View.VISIBLE);
//            pwdLayout1.setVisibility(View.VISIBLE);
            pwdLayout2.setVisibility(View.VISIBLE);
            title.setText(R.string.reset_login_pwd);
        }
        if(verify){
            title.setText(R.string.verify);
        }
        mPhone = intent.getStringExtra(PHONE);
        getContry();
        handleros.sendEmptyMessage(TIME);
//        SMSSDK.registerEventHandler(eh);
//        checkPermissions();
    }

    @Event(value = R.id.comfirm)
    private void comfirm(View view) throws DbException {
        verify();
    }
    @Event(value = R.id.back_btn)
    private void back_btn(View view) throws DbException {
        finish();
    }

    @Event(value = R.id.get_code)
    private void get_code(View view) throws DbException {
        mGetCodeTextView.setEnabled(false);
        resend();
    }
    @Event(value = R.id.phone_code_layout)
    private void phone_code_layout(View view) throws DbException {
        showCountryDialog();
    }

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
                        mGetCodeTextView.setText(time + getResources().getString(R.string.get_code_again_time));
                        mGetCodeTextView.setTextColor(getResources().getColor(R.color.grey_color));
                        time--;
                        handleros.sendEmptyMessageDelayed(TIME, delayed);
                    } else {
                        mGetCodeTextView.setText(R.string.get_code_again);
                        mGetCodeTextView.setTextColor(getResources().getColor(R.color.theme_color));
                        mGetCodeTextView.setEnabled(true);
                        time = 60;
                    }
                    break;
                case TRY_SUCESS:
//                    login();
                    break;
                case TRY_FAIL:
                    String error = (String) msg.obj;
                    Toast.makeText(VerifyActivity.this, error, Toast.LENGTH_SHORT).show();
                    mGetCodeTextView.setEnabled(true);
                    break;
            }
        }
    };

    private void verify() {
        String code = codeEdit.getText().toString();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, R.string.code_not_null, Toast.LENGTH_SHORT).show();
            return;
        }
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.BUSER_URL);
        String action = "signup_verify";
        if (VERIFY_TYPE == FORGET_PWD_TYPE) {
            action = "reset";
            String pwd = pwdEdit.getText().toString();
            if (!PwdCheckUtil.isContainAll(pwd) || pwd.length() < 8) {
                Toast.makeText(this, R.string.pwd_rule, Toast.LENGTH_SHORT).show();
                return;
            }
            String pwd1 = pwdEdit1.getText().toString();
            if (!pwd1.equals(pwd)) {
                Toast.makeText(this, R.string.pwd_diffrence, Toast.LENGTH_SHORT).show();
                return;
            }
            params.addParameter("password", pwd);
            params.addParameter("country", countryName);
        } else if (VERIFY_TYPE == LOGIN_TYPE) {
            action = "signin_verify";
            params.addParameter("mobile", mPhone);
        }
        params.addParameter("action", action);
        params.addParameter("otp", code);
        params.addParameter("temp_token", token);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                hideProgress();
                if (VERIFY_TYPE == LOGIN_TYPE) {
                    BaseCode<BitLoginInfo> base = new Gson().fromJson(
                            result,
                            new TypeToken<BaseCode<BitLoginInfo>>() {
                            }.getType());
                    if (base.getCode() == 0) {
                        BitLoginInfo info = base.getData();
//                        MyHttpManager.token = info.getToken();
//                        MyHttpManager.registerInstance();
                        SPUtils.put("token",info.getToken());
                        SPUtils.put("rToken",info.getrToken());
                        SPUtils.put("bToken",info.getbToken());
                        SPUtils.put("name",info.getName());
                        SPUtils.put("phone",info.getPhone());
                        SPUtils.put(LOGIN_PAY, base.getData().isPay());
                        SPUtils.put(USER_ICON, base.getData().getIcon());
                        SPUtils.put(USER_PHONE, base.getData().getPhone());
                        SPUtils.put(USER_NAME, base.getData().getName());
                        SPUtils.put(LOGIN_TOKEN, info.getrToken());
//                    SPUtils.put(LOGIN_TOKEN, UrlUtil.getURLEncoderString(base.getData().getToken()));
                        sendBroadcast(new Intent(ACTION_PAY));
                        Intent intent = new Intent(VerifyActivity.this,MainActivity.class);
                        intent.putExtra(FRESH,true);
//                        EventBus.getDefault().post(new Close());
                        startActivity(intent);
                        finish();
                    } else {
                        String s = base.getMessage();
                        if(s.contains("Invalid Token")){
                            showToast("Token过期");
                            finish();
                            return;
                        }
                        if(base.getMessage().contains("Inva")){
                            s="验证码错误";
                        }
                        Toast.makeText(VerifyActivity.this,s, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    BaseCode<String> base = new Gson().fromJson(
                            result,
                            new TypeToken<BaseCode<String>>() {
                            }.getType());

                    if (base.getCode() == 0) {
                        setResultInfo(VERIFY_TYPE);
                    } else {
                        String s = base.getMessage();
                        if(s.contains("Invalid Token")){
                            showToast("Token过期");
                            finish();
                            return;
                        }
                        if(base.getMessage().contains("Inva")){
                            s="验证码错误";
                        }
                        Toast.makeText(VerifyActivity.this,s, Toast.LENGTH_SHORT).show();
                    }

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

    private void setResultInfo(int code) {
        Intent intent = new Intent();
        setResult(code, intent);
        finish();
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
                    mCountryCode = content.getTel();
                    countryName = content.getName();
                    mCodeView.setText(content.getCn()+"     "+mCountryCode);
                        mCountryDialog.dismiss();
                }
            });
            return view;
        }
    }

    class ViewHolder {
        private TextView countryTextView;
        private TextView codeTextView;
    }

    private void resend() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.BUSER_URL);
        String action = "signup_resend";
        if (VERIFY_TYPE == FORGET_PWD_TYPE) {
            action = "forget";
            params.addParameter("mobile", mPhone);
            params.addParameter("country", countryName);
        } else if (VERIFY_TYPE == LOGIN_TYPE) {
            action = "signin_resend";
            params.addParameter("mobile", mPhone);
        }
        params.addParameter("action", action);
        params.addParameter("temp_token", token);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                hideProgress();
                BaseCode<String> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<String>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    handleros.sendEmptyMessage(TIME);
                    token = base.getData();
                } else {
                    Toast.makeText(VerifyActivity.this, base.getMessage(), Toast.LENGTH_LONG).show();
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

    private void getContry() {
//        RequestParams params = new RequestParams("https://www.lesson1234.com:9443/pp/country/country.json");
//        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
//            @Override
//            public void onSuccess(String result) {
//                hideProgress();
//                List<ContryCode> datas = new Gson().fromJson(
//                        result,
//                        new TypeToken<List<ContryCode>>() {
//                        }.getType());
//                contryCodes.addAll(datas);
//            }
//
//
//            @Override
//            public void onError(Throwable ex, boolean isOnCallback) {
//                ex.printStackTrace();
//            }
//
//
//            @Override
//            public void onCancelled(CancelledException cex) {
//                cex.printStackTrace();
//            }
//
//
//            @Override
//            public void onFinished() {
//            }
//        });
    }

    private Dialog mCountryDialog;

    private void showCountryDialog() {
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
}
  