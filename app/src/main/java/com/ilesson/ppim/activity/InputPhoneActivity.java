package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
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
import com.ilesson.ppim.entity.Close;
import com.ilesson.ppim.entity.ContryCode;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.PwdCheckUtil;

import org.xutils.common.Callback;
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
import io.rong.eventbus.EventBus;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by potato on 2020/3/5.
 */
@ContentView(R.layout.activity_input_phone)
public class InputPhoneActivity extends BaseActivity{

    @ViewInject(R.id.phone_edit)
    private EditText phoneEdit;
    @ViewInject(R.id.phone_code)
    private TextView mCodeView;
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.registe)
    private View registe;
    private List<ContryCode> contryCodes;
    private String countryCode = "86";
    private String countryName = "CN";
    private static final String TAG = "RegisteActivity";
    public static final String TYPE_VERIFY = "type_verify";
    private String phone;
    private boolean verify;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setStatusBarLightMode(this,true);
        verify = getIntent().getBooleanExtra(TYPE_VERIFY,false);
        if(verify){
            title.setText(R.string.verify);
        }
        contryCodes = new ArrayList<>();
        getContry();
//        SMSSDK.registerEventHandler(eh);
//        checkPermissions();
    }
    @Event(value = R.id.registe)
    private void registe(View view) throws DbException {
        registe();
    }
    @Event(value = R.id.back_btn)
    private void back_btn(View view) throws DbException {
        finish();
    }

    @Event(value = R.id.phone_code_layout)
    private void phone_code_layout(View view) throws DbException {
        showCountryDialog();
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
//        RequestParams params = new RequestParams("https://www.lesson1234.com:9443/pp/country/country.json");
//        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
//            @Override
//            public void onSuccess(String result) {
//                hideProgress();
//                List<ContryCode> datas = new Gson().fromJson(
//                        result,
//                        new TypeToken<List<ContryCode>>() {
//                        }.getType());
//                contryCodes.clear();
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

    private static final int TIME = 0;
    private static final int TRY_SUCESS = 1;
    private static final int TRY_FAIL = 2;
    private int time = 60;
    private long delayed = 1000;
    public void onEventMainThread(Close close) {
        if(!isFinishing())
            finish();
    }
    private void registe() {
        registe.setEnabled(false);
        final String phone = phoneEdit.getText().toString();
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this,R.string.phone_not_null, Toast.LENGTH_SHORT).show();
            return;
        }
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.BUSER_URL);
        params.addBodyParameter("action", "forget");
        params.addParameter("mobile", phone);
        params.addParameter("country", countryName);
        Log.d(TAG, "loadData: "+params.toString());
        showProgress();
        org.xutils.x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                BaseCode<String> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<String>>() {
                        }.getType());
                Log.d(TAG, "onSuccess: +" + result);
                hideProgress();
                if (base.getCode() == 0) {
                    Intent intent = new Intent(InputPhoneActivity.this,VerifyActivity.class);
                    intent.putExtra(VerifyActivity.TEMP_TOKEN,base.getData());
                    intent.putExtra(TYPE_VERIFY,verify);
                    intent.putExtra(VerifyActivity.PHONE,phone);
                    intent.putExtra(VerifyActivity.VERIFY_ACTION,VerifyActivity.FORGET_PWD_TYPE);
                    startActivityForResult(intent,0);
                } else  {
                    Toast.makeText(InputPhoneActivity.this,"手机号和国家码不符", Toast.LENGTH_SHORT).show();
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
                registe.setEnabled(true);
                hideProgress();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==VerifyActivity.FORGET_PWD_TYPE){
            setResult(VerifyActivity.FORGET_PWD_TYPE);
            finish();
        }
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
  