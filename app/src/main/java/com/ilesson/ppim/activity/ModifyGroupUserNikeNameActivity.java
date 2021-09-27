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

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.RoundImageView;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupUserInfo;

import static com.ilesson.ppim.activity.AvatarActivity.MODIFY_SUCCESS;
import static com.ilesson.ppim.activity.ChatInfoActivity.GROUP_ICON;
import static com.ilesson.ppim.activity.ChatInfoActivity.GROUP_ID;
import static com.ilesson.ppim.activity.ChatInfoActivity.NIKE_NAME;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_modify_group_user_nikename)
public class ModifyGroupUserNikeNameActivity extends BaseActivity {
    @ViewInject(R.id.save)
    private TextView saveBtn;
    //    @ViewInject(R.id.group_name)
//    private TextView groupName;
    @ViewInject(R.id.icon)
    private RoundImageView iconView;
    @ViewInject(R.id.nike_edit)
    private EditText nikeEdit;
    public static final int MODIFY_NAME = 0;
    public static final int MODIFY_GROUP = 1;
    public static final int MODIFY_REAL_NAME = 2;
    public static final int MODIFY_SYMBL = 3;
    public static final int MODIFY_NIKE_IN_GROUP = 4;
    public static final String MODIFY_CONTENT = "modify_content";
    public static final String MODIFY_TYPE = "modify_type";
    public static final String MODIFY_RESULT = "modify_result";
    private int type;
    private String groupId;
    private String realName;
    private String nameSymbl;

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this, true);
        String name = getIntent().getStringExtra(MODIFY_CONTENT);
        groupId = getIntent().getStringExtra(GROUP_ID);
        String groupIcon = getIntent().getStringExtra(GROUP_ICON);
        Glide.with(getApplicationContext()).load(groupIcon).into(iconView);
        saveBtn.setEnabled(false);
        nikeEdit.setText(name);
        nikeEdit.setSelection(name.length());
        nikeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    saveBtn.setTextColor(getResources().getColor(R.color.color_999999));
                    saveBtn.setBackgroundResource(R.drawable.background_gray_corner20);
                } else {
                    saveBtn.setEnabled(true);
                    saveBtn.setTextColor(getResources().getColor(R.color.white));
                    saveBtn.setBackgroundResource(R.drawable.background_theme_corner20);
                }
            }
        });
    }

    @Event(R.id.save)
    private void save(View view) {
        String name = nikeEdit.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            return;
        }
        modifyGroupUserNike(name);
    }

    private static final String TAG = "ModifyNikeNameActivity";

    private void modifyGroupUserNike(String name) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.GROUP_URL);
        String token = SPUtils.get(LOGIN_TOKEN, "");
        params.addParameter("token", token);
        params.addParameter("action", "modify_my_name");
        params.addParameter("group", groupId);
        params.addParameter("name", name);
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
                if (base.getCode() == 0) {
                    Intent intent = new Intent();
                    intent.putExtra(MODIFY_RESULT, name);
                    SPUtils.put(NIKE_NAME, name);
                    setResult(MODIFY_SUCCESS, intent);
                    GroupUserInfo groupUserInfo = new GroupUserInfo(groupId,SPUtils.get(LoginActivity.USER_PHONE, ""),name);
                    RongIM.getInstance().refreshGroupUserInfoCache(groupUserInfo);
                    finish();
                } else {
                    Toast.makeText(ModifyGroupUserNikeNameActivity.this, base.getMessage(), Toast.LENGTH_LONG).show();
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
    private void back_btn(View view) {
        finish();
    }

}
