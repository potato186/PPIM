package com.ilesson.ppim.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
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
import com.ilesson.ppim.entity.BitLoginInfo;
import com.ilesson.ppim.entity.ContryCode;
import com.ilesson.ppim.update.UpdateHelper;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.common.util.MD5;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import static com.ilesson.ppim.activity.MainActivity.FRESH;
import static com.ilesson.ppim.activity.ResetPwdActivity.RESET_LOGIN_PWD;
import static com.ilesson.ppim.activity.UserDetailActivity.BIRTH;
import static com.ilesson.ppim.activity.UserDetailActivity.SEX;
import static com.ilesson.ppim.fragment.FundFragment.ACTION_PAY;

/**
 * Created by potato on 2020/3/5.
 */
@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity {

    @ViewInject(R.id.phone_edit)
    private EditText phoneEdit;
    @ViewInject(R.id.pwd_edit)
    private EditText pwdEdit;
    @ViewInject(R.id.phone_code)
    private TextView mCodeView;
    private String prefix = "86";
    private static final String TAG = "LoginActivity";
    public static final String LOGIN_TOKEN = "login_token";
    public static final String LOGIN_ACTION = "login_action";
    public static final String LOGIN_PAY = "login_pay";
    public static final String USER_MONEY = "user_money";
    public static final String USER_ICON = "user_icon";
    public static final String TOKEN = "token";
    public static final String USER_PHONE = "login_user_phone";
    public static final String USER_SCORE = "user_score";
    public static final String USER_NAME = "login_user_name";
    public static final String REAL_NAME = "real_name";
    public static final String NAME_SYMBL = "name_symbl";
    public static final String AGREE_PRIVACY = "agree_privacy";
    private String countryCode = "86";
    private String countryName = "CN";
    private List<ContryCode> contryCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this, true);
        new UpdateHelper(this).checkVersion(false);
        getContry();
        boolean agree = SPUtils.get(AGREE_PRIVACY,false);
        if(!agree){
            showPrivacyDialog();
        }
    }

    @Event(value = R.id.close)
    private void back_btn(View view) throws DbException {
        System.exit(0);
        finish();
    }
    @Event(value = R.id.login)
    private void login(View view) throws DbException {
        login();
    }

    @Event(value = R.id.forget)
    private void forget(View view) throws DbException {
        resetPwd();
    }

    @Event(value = R.id.phone_code_layout)
    private void phone_code_layout(View view) throws DbException {
        if(contryCodes==null||contryCodes.size()==0){
            getContry();
        }
        showCountryDialog();
    }

    @Event(value = R.id.registe)
    private void registe(View view) {
        startActivity(new Intent(LoginActivity.this, RegisteActivity.class));
    }

    private void resetPwd() {
        Intent intent = new Intent(LoginActivity.this, ResetPwdActivity.class);
        intent.putExtra(RESET_LOGIN_PWD, true);
        startActivity(intent);
    }

    //action=register&prefix=%s&phone=%s&key=%s&name=%s&code=%s
    private void login() {
        final String phone = phoneEdit.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "手机号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String pwd = pwdEdit.getText().toString();
        if (TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.BUSER_URL);
        params.addParameter("action", "signin_v3");
        params.addParameter("country", countryName);
        params.addParameter("mobile", phone);
        params.addParameter("password", MD5.md5(pwd));
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                hideProgress();
                BaseCode<BitLoginInfo> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<BitLoginInfo>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    BitLoginInfo info = base.getData();
//                        MyHttpManager.token = info.getToken();
//                        MyHttpManager.registerInstance();
                    SPUtils.put(TOKEN, info.getToken());
                    SPUtils.put("bToken", "");
                    SPUtils.put(USER_PHONE, phone);
                    SPUtils.put(LOGIN_PAY, info.isPay());
                    SPUtils.put(USER_ICON, info.getIcon());
                    SPUtils.put(USER_NAME, info.getName());
                    SPUtils.put(LOGIN_TOKEN, info.getrToken());
                    if(!TextUtils.isEmpty(info.getRealName())){
                        SPUtils.put(REAL_NAME,info.getRealName());
                    }
                    if(!TextUtils.isEmpty(info.getRealNameSymbol())){
                        SPUtils.put(NAME_SYMBL, info.getRealNameSymbol());
                    }
                    if(!TextUtils.isEmpty(info.getBirthday())){
                        SPUtils.put(BIRTH,info.getBirthday());
                    }
                    if(!TextUtils.isEmpty(info.getSex())){
                        SPUtils.put(SEX,info.getSex());
                    }
//                    SPUtils.put(LOGIN_TOKEN, UrlUtil.getURLEncoderString(base.getData().getToken()));
                    sendBroadcast(new Intent(ACTION_PAY));
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra(FRESH, true);
//                        EventBus.getDefault().post(new Close());
                    startActivity(intent);
                    finish();
                } else if (base.getCode() == -1) {
                    resetPwd();
                } else if (base.getCode() == -2) {
                    showToast(base.getMessage());
                } else if (base.getCode() == -3) {
                    showToast(base.getMessage());
                } else {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
                    mCodeView.setText(content.getCn() + "     " + countryCode);
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

    private void showPrivacyDialog(){
        View view = getLayoutInflater().inflate(R.layout.privacy_dialog,null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view).create();
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(R.string.private_title);
        TextView scoreTv = (TextView) view.findViewById(R.id.content);
        String tips = getResources().getString(R.string.privacy_tips);
        scoreTv.setText(tips);
        TextView disagree = (TextView) view.findViewById(R.id.left_btn);
        TextView agree = (TextView) view.findViewById(R.id.right_btn);
        SpannableString ss = new SpannableString(tips);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.theme_color));
        ClickableSpan clickDisgree = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                privacy(true);
            }
        };
        UnderlineSpan underlineSpan = new UnderlineSpan(){
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.theme_color));
                ds.setUnderlineText(false);
            }
        };
        UnderlineSpan underlineSpan1 = new UnderlineSpan(){
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.theme_color));
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickDisgree,34,40, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        ss.setSpan(underlineSpan,34,40, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        ClickableSpan clickAgree = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                privacy(false);
            }
        };
        ss.setSpan(clickAgree,41,47, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        ss.setSpan(underlineSpan1,41,47, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        scoreTv.setText(ss);
        scoreTv.setMovementMethod(LinkMovementMethod.getInstance());
        disagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                SPUtils.put(AGREE_PRIVACY,true);
            }
        });
        dialog.show();
    }
    private void privacy(boolean privacy){
        Intent intent = new Intent(this, PrivateActivity.class);
        intent.putExtra(PrivateActivity.USER_POLICE,privacy);
        startActivity(intent);
    }
}
  