package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.DeleteFriend;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.entity.RongUserInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

import io.rong.eventbus.EventBus;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.ForwadSelectActivity.INTENT_TYPE;
import static com.ilesson.ppim.activity.ForwadSelectActivity.SEND_FRIEND_CARD;
import static com.ilesson.ppim.activity.FriendDetailActivity.USER_ID;
import static com.ilesson.ppim.activity.FriendDetailActivity.USER_INFO;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_user_info_set)
public class UserInfoSttingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener{
    @ViewInject(R.id.backlist_switch)
    private Switch backlistSwitch;
    private static final String TAG = "UserSttingActivity";
    private String name;
    private String nikeName="";
    private String userId;
    private boolean modifyed;
    private PPUserInfo addUser,deleteUser;
    private String groupIcon;
    private PPUserInfo ppUserInfo;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        userId = getIntent().getStringExtra(USER_ID);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        ppUserInfo = (PPUserInfo) bundle.getSerializable(USER_INFO);
        backlistSwitch.setOnCheckedChangeListener(UserInfoSttingActivity.this);
        blackListQuery();
    }

    @Event(R.id.back_btn)
    private void back_btn(View view){
        finish();
    }
    @Event(R.id.delete)
    private void delete(View view){
        showTipDialog(true);
    }
    @Event(R.id.recommend_layout)
    private void recommend(View view){
        Intent intent = new Intent(this, ForwadSelectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(FriendDetailActivity.USER_INFO, ppUserInfo);
        intent.putExtra(INTENT_TYPE,SEND_FRIEND_CARD);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void showTipDialog(boolean delete){
        View view = getLayoutInflater().inflate(R.layout.practice_dialog,null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
        TextView textView = view.findViewById(R.id.content);
        TextView title =  view.findViewById(R.id.title);
        title.setVisibility(View.VISIBLE);
        if(delete){
            textView.setText(String.format(getResources().getString(R.string.delete_friend_tips),ppUserInfo.getName()));
            title.setText(R.string.delete_friend_title);
        }else{
            textView.setText(R.string.add_black_list_tip);
            title.setText(R.string.add_black_list);
        }
        view.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                backlistSwitch.setOnCheckedChangeListener(null);
                backlistSwitch.setChecked(false);
                backlistSwitch.setOnCheckedChangeListener(UserInfoSttingActivity.this);
            }
        });
        view.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(delete){
                    deleteUser();
                }else{
                    blackListAdd();
                }
            }
        });
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (PPScreenUtils.getScreenWidth(this) * 0.85);
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setAttributes(p);
        dialog.show();
    }
    private void blackListAdd() {
        String token = SPUtils.get(LoginActivity.LOGIN_TOKEN,"");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addParameter("action", "black_list_add");
        params.addParameter("token", token);
        params.addParameter("target", ppUserInfo.getPhone());
        showProgress();
        Log.d(TAG, "requestGroupInfo: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: =" + result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if (base.getCode() == 0) {
                    backlistSwitch.setOnCheckedChangeListener(null);
                    backlistSwitch.setChecked(true);
                    backlistSwitch.setOnCheckedChangeListener(UserInfoSttingActivity.this);
                    isBlack=true;
                } else {
                }
                Toast.makeText(UserInfoSttingActivity.this,base.getMessage(),Toast.LENGTH_LONG).show();
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
    private boolean isBlack;
    private void blackListQuery() {
        String token = SPUtils.get(LoginActivity.LOGIN_TOKEN,"");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addParameter("action", "black_list_query");
        params.addParameter("token", token);
        params.addParameter("target", ppUserInfo.getPhone());
        showProgress();
        Log.d(TAG, "requestGroupInfo: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: =" + result);
                BaseCode<List<RongUserInfo>> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<List<RongUserInfo>>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    List<RongUserInfo> list = base.getData();
                    for (RongUserInfo info : list) {
                        if(info.getId().equals(ppUserInfo.getPhone())){
                            backlistSwitch.setOnCheckedChangeListener(null);
                            backlistSwitch.setChecked(true);
                            isBlack = true;
                            backlistSwitch.setOnCheckedChangeListener(UserInfoSttingActivity.this);
                            return;
                        }
                    }
                } else {
                    Toast.makeText(UserInfoSttingActivity.this,base.getMessage(),Toast.LENGTH_LONG).show();
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
    private void blackListRemove() {
        String token = SPUtils.get(LoginActivity.LOGIN_TOKEN,"");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addParameter("action", "black_list_remove");
        params.addParameter("token", token);
        params.addParameter("target", ppUserInfo.getPhone());
        showProgress();
        Log.d(TAG, "requestGroupInfo: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: =" + result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if (base.getCode() == 0) {
                    isBlack = false;
                    backlistSwitch.setOnCheckedChangeListener(null);
                    backlistSwitch.setChecked(false);
                    backlistSwitch.setOnCheckedChangeListener(UserInfoSttingActivity.this);
                } else {
                    Toast.makeText(UserInfoSttingActivity.this,base.getMessage(),Toast.LENGTH_LONG).show();
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
    private void deleteUser() {
        String token = SPUtils.get(LoginActivity.LOGIN_TOKEN,"");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addParameter("action", "friend_delete");
        params.addParameter("token", token);
        params.addParameter("target", ppUserInfo.getPhone());
        showProgress();
        Log.d(TAG, "requestGroupInfo: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: =" + result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if (base.getCode() == 0) {
                    EventBus.getDefault().post(new DeleteFriend(ppUserInfo.getPhone()));
                    RongIM.getInstance().removeConversation(Conversation.ConversationType.PRIVATE,ppUserInfo.getPhone(),null);
                    finish();
                } else {
                    Toast.makeText(UserInfoSttingActivity.this,base.getMessage(),Toast.LENGTH_LONG).show();
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            showTipDialog(false);
        }else{
            blackListRemove();
        }
    }
}
