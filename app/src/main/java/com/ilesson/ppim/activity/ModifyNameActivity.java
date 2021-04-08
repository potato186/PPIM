package com.ilesson.ppim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import static com.ilesson.ppim.activity.AvatarActivity.MODIFY_SUCCESS;
import static com.ilesson.ppim.activity.ChatInfoActivity.GROUP_ID;
import static com.ilesson.ppim.activity.ChatInfoActivity.GROUP_NAME;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_NAME;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_modify_name)
public class ModifyNameActivity extends BaseActivity{
    @ViewInject(R.id.save)
    private TextView saveBtn;
    @ViewInject(R.id.group_name)
    private TextView groupName;
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.nike_edit)
    private EditText nikeEdit;
    public static final int MODIFY_PERSON=0;
    public static final int MODIFY_GROUP=1;
    public static final String MODIFY_TYPE="modify_type";
    public static final String MODIFY_RESULT="modify_result";
    private int type;
    private String groupId;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        type = getIntent().getIntExtra(MODIFY_TYPE,0);
        groupId = getIntent().getStringExtra(GROUP_ID);
        String name = getIntent().getStringExtra(GROUP_NAME);
        String text="";
        if(type==MODIFY_GROUP){
            title.setText(R.string.modify_group_name);
            groupName.setVisibility(View.VISIBLE);
            text = name;
        }else{
            text = SPUtils.get(USER_NAME,"");
        }
        nikeEdit.setText(text);
        nikeEdit.setSelection(text.length());
        nikeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0){
                    saveBtn.setTextColor(getResources().getColor(R.color.color_999999));
                    saveBtn.setBackgroundResource(R.drawable.background_gray_corner5);
                }else{
                    saveBtn.setTextColor(getResources().getColor(R.color.white));
                    saveBtn.setBackgroundResource(R.drawable.theme_gray_corer5_btn_selector);
                }
            }
        });
    }

    @Event(R.id.save)
    private void save(View view){
        String name = nikeEdit.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            return;
        }
        if(type==MODIFY_GROUP){
            modifyGroup(name);
            return;
        }
        modify(name);
    }

    private static final String TAG = "ModifyNameActivity";
    private void modify(final String name) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        String token = SPUtils.get(LOGIN_TOKEN,"");
        params.addQueryStringParameter("token", token);
        params.addQueryStringParameter("action", "modify");
        params.addQueryStringParameter("name", name);
        params.setMultipart(true);
        showProgress();
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<PPUserInfo> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<PPUserInfo>>() {
                        }.getType());
                if(base.getCode()==0){
                    SPUtils.put(USER_NAME,name);
                    Intent intent = new Intent();
                    setResult(MODIFY_SUCCESS,intent);
                    finish();
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
    private void modifyGroup(final String name) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.GROUP_URL);
        String token = SPUtils.get(LOGIN_TOKEN,"");
        params.addQueryStringParameter("token", token);
        params.addQueryStringParameter("action", "rename");
        params.addQueryStringParameter("group", groupId);
        params.addQueryStringParameter("name", name);
        params.setMultipart(true);
        showProgress();
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
                    Intent intent = new Intent();
                    intent.putExtra(MODIFY_RESULT,name);
                    SPUtils.put(LoginActivity.USER_NAME,name);
                    setResult(MODIFY_SUCCESS,intent);
                    finish();
                }else{
                    Toast.makeText(ModifyNameActivity.this,base.getMessage(),Toast.LENGTH_LONG).show();
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
    @Event(R.id.back_btn)
    private void back_btn(View view){
        finish();
    }

}
