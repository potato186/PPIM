package com.ilesson.ppim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.MD5;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.VirtualKeyboardView;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_PAY;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.fragment.FundFragment.FUND_NOT_ACTIVED;

public class PayPwdActivity extends BaseActivity {

    private VirtualKeyboardView virtualKeyboardView;

    private GridView gridView;

    private ArrayList<Map<String, String>> valueList;

    private EditText textAmount;

    private Animation enterAnim;

    private Animation exitAnim;
    private EditText mCode1, mCode2, mCode3, mCode4, mCode5, mCode6;
    private EditText[] mCodes;
    private String firstPwd;
    public static final String ACTIVE_PASSWORD = "active_password";
    private boolean active;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_pwd);
        active = getIntent().getBooleanExtra(ACTIVE_PASSWORD,false);
        setStatusBarLightMode(this,true);
        initAnim();

        initView();

        valueList = virtualKeyboardView.getValueList();
    }

    /**
     * 数字键盘显示动画
     */
    private void initAnim() {
        enterAnim = AnimationUtils.loadAnimation(this, R.anim.push_bottom_in);
        exitAnim = AnimationUtils.loadAnimation(this, R.anim.push_bottom_out);
    }

    private TextView tipView;

    private void initView() {
        tipView = findViewById(R.id.msg_tip);
        textAmount = (EditText) findViewById(R.id.textAmount);
        mCode1 = findViewById(R.id.code1);
        mCode2 = findViewById(R.id.code2);
        mCode3 = findViewById(R.id.code3);
        mCode4 = findViewById(R.id.code4);
        mCode5 = findViewById(R.id.code5);
        mCode6 = findViewById(R.id.code6);
        mCodes = new EditText[]{mCode1, mCode2, mCode3, mCode4, mCode5, mCode6};
        for (int i = 0; i < mCodes.length; i++) {
            final EditText editText = mCodes[i];
            final int index = i;
            editText.setCursorVisible(false);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        }
        // 设置不调用系统键盘
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            textAmount.setInputType(InputType.TYPE_NULL);
        } else {
            this.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            try {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus",
                        boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(textAmount, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        virtualKeyboardView = (VirtualKeyboardView) findViewById(R.id.virtualKeyboardView);
        virtualKeyboardView.getLayoutBack().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                virtualKeyboardView.startAnimation(exitAnim);
                virtualKeyboardView.setVisibility(View.GONE);
            }
        });

        gridView = virtualKeyboardView.getGridView();
        gridView.setOnItemClickListener(onItemClickListener);

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = check();
                if (pwd.length() != 6) {
                    Toast.makeText(PayPwdActivity.this, R.string.input_six_length_pwd, Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(firstPwd)) {
                    firstPwd = pwd;
                    tipView.setText(R.string.input_pwd_again);
                    clearChar();
                    return;
                }
                if(!pwd.equals(firstPwd)){
                    Toast.makeText(PayPwdActivity.this, R.string.error_pwd, Toast.LENGTH_LONG).show();
                    return;
                }
                if(active){
                    active();
                }else{
                    setPayPwd();
                }
            }
        });

    }

    private void active() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.BUSER_ASSET);
        params.addParameter("action", "register");
        params.addParameter("token", SPUtils.get("token",""));
        params.addBodyParameter("pwd", MD5.md5(firstPwd));
        showProgress();
        Log.d(TAG, "loadData: " + params.toString());
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                hideProgress();
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<String> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<String>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    SPUtils.put(FUND_NOT_ACTIVED,false);
                    SPUtils.put(LOGIN_PAY, true);
                    setActiveResult();
                }
                else if(base.getCode() == -1) {
                    Toast.makeText(PayPwdActivity.this, base.getMessage(), Toast.LENGTH_LONG).show();
                }
                else if(base.getCode() == -2) {
                    Toast.makeText(PayPwdActivity.this, "网络错误", Toast.LENGTH_LONG).show();
                }
                else if(base.getCode() == -3) {
                    Intent intent = new Intent(PayPwdActivity.this,CustomerActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(PayPwdActivity.this, base.getMessage(), Toast.LENGTH_LONG).show();
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
                PayPwdActivity.this.hideProgress();
            }
        });
    }
    private void setActiveResult(){
        Intent intent = new Intent();
        setResult(MainActivity.ACTIVE_SUCCESS, intent);
        finish();
    }
    private static final String TAG = "PayPwdActivity";
    private void setPayPwd() {
        //action=friend_pre&token=q6TqTmCJVngz3lt%2bloDqEfKjlVc0nPCvkB
        String token = SPUtils.get(LOGIN_TOKEN, "");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.ASSET_URL);
        params.addBodyParameter("action", "pay_pwd");
        params.addBodyParameter("token", token);
        params.addBodyParameter("pwd", MD5.md5(firstPwd));
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if(base.getCode()==0){
                    Toast.makeText(PayPwdActivity.this, R.string.set_pwd_success, Toast.LENGTH_LONG).show();
                    SPUtils.put(LOGIN_PAY, true);
                    finish();
                }else{
                    Toast.makeText(PayPwdActivity.this, base.getMessage(), Toast.LENGTH_LONG).show();
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
            }
        });
    }
    private void addChar(String str) {
        for (int i = 0; i < mCodes.length; i++) {
            if (TextUtils.isEmpty(mCodes[i].getText().toString())) {
                mCodes[i].setText(str);
                break;
            }
        }
    }

    private void clearChar() {
        for (int i = 0; i < mCodes.length; i++) {
            mCodes[i].setText("");
        }
    }

    private void deleteChar() {
        for (int i = mCodes.length - 1; i >= 0; i--) {
            if (!TextUtils.isEmpty(mCodes[i].getText().toString())) {
                mCodes[i].setText("");
                break;
            }
        }
    }

    private String check() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mCodes.length; i++) {
            stringBuilder.append(mCodes[i].getText().toString());
        }
        return stringBuilder.toString();
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            if (position < 11 && position != 9) {    //点击0~9按钮

//                String amount = textAmount.getText().toString().trim();
                String amount = valueList.get(position).get("name");
//
//                textAmount.setText(amount);
//
//                Editable ea = textAmount.getText();
//                textAmount.setSelection(ea.length());
                addChar(amount);
            } else {

//                if (position == 9) {      //点击退格键
//                    String amount = textAmount.getText().toString().trim();
//                    if (!amount.contains(".")) {
//                        amount = amount + valueList.get(position).get("name");
//                        textAmount.setText(amount);
//
//                        Editable ea = textAmount.getText();
//                        textAmount.setSelection(ea.length());
//                    }
//                }

                if (position == 11) {      //点击退格键
//                    String amount = textAmount.getText().toString().trim();
//                    if (amount.length() > 0) {
//                        amount = amount.substring(0, amount.length() - 1);
//                        textAmount.setText(amount);
//
//                        Editable ea = textAmount.getText();
//                        textAmount.setSelection(ea.length());
//                    }
                    deleteChar();
                }
            }
        }
    };
}
