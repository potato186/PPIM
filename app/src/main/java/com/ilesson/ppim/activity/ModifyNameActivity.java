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
import com.ilesson.ppim.utils.TextUtil;

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
import static com.ilesson.ppim.activity.LoginActivity.NAME_SYMBL;
import static com.ilesson.ppim.activity.LoginActivity.REAL_NAME;
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
    public static final int MODIFY_NAME=0;
    public static final int MODIFY_GROUP=1;
    public static final int MODIFY_REAL_NAME=2;
    public static final int MODIFY_SYMBL=3;
    public static final int MODIFY_NIKE_IN_GROUP=4;
    public static final String MODIFY_CONTENT="modify_content";
    public static final String MODIFY_TYPE="modify_type";
    public static final String MODIFY_RESULT="modify_result";
    private int type;
    private String groupId;
    private String realName;
    private String nameSymbl;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        type = getIntent().getIntExtra(MODIFY_TYPE,0);
        groupId = getIntent().getStringExtra(GROUP_ID);
        String gName = getIntent().getStringExtra(GROUP_NAME);
        String name = getIntent().getStringExtra(MODIFY_CONTENT);
        String text="";
        switch (type){
            case MODIFY_GROUP:
                title.setText(R.string.modify_group_name);
                groupName.setVisibility(View.VISIBLE);
                text = gName;
                break;
            case MODIFY_NAME:
                text = SPUtils.get(USER_NAME,"");
                break;
            case MODIFY_REAL_NAME:
                text = SPUtils.get(REAL_NAME,"");
                realName = text;
                title.setText(R.string.modify_real_name);
                break;
            case MODIFY_SYMBL:
                text = SPUtils.get(NAME_SYMBL,"");
                nameSymbl = text;
                title.setText(R.string.modify_real_name_symbol);
                break;
            case MODIFY_NIKE_IN_GROUP:
                text =name;
                title.setText(R.string.nike_in_group);
                break;
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
        if(type==MODIFY_NIKE_IN_GROUP){
            modifyGroupUserNike(name);
            return;
        }
        modify(name);
    }

    private static final String TAG = "ModifyNameActivity";
    private void modify(final String name) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        String token = SPUtils.get(LOGIN_TOKEN,"");
        params.addParameter("token", token);
        if(type==MODIFY_GROUP||type==MODIFY_NAME){
            params.addParameter("action", "modify");
            params.addParameter("name", name);
        }else if(type==MODIFY_REAL_NAME){
            params.addParameter("action", "mod_rname");
            params.addParameter("real_name", name);
            params.addParameter("name_symbol", TextUtil.getPinyin(name));
        }else{
            params.addParameter("action", "mod_rname");
            params.addParameter("real_name", SPUtils.get(USER_NAME,""));
            params.addParameter("name_symbol", name);
        }
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
                    if(type==MODIFY_NAME){
                        SPUtils.put(USER_NAME,name);
                    }else if(type==MODIFY_REAL_NAME){
                        SPUtils.put(REAL_NAME,name);
                        SPUtils.put(NAME_SYMBL,TextUtil.getPinyin(name));
                    }else if(type==MODIFY_SYMBL){
                        SPUtils.put(NAME_SYMBL,name);
                    }
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
        params.addParameter("token", token);
        params.addParameter("action", "rename");
        params.addParameter("group", groupId);
        params.addParameter("name", name);
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
    private void modifyGroupUserNike(String name) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.GROUP_URL);
        String token = SPUtils.get(LOGIN_TOKEN,"");
        params.addParameter("token", token);
        params.addParameter("action", "modify_my_name");
        params.addParameter("group", groupId);
        params.addParameter("name", name);
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
