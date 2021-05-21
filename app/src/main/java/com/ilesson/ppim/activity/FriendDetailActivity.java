package com.ilesson.ppim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.IlessonApp;
import com.ilesson.ppim.R;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.entity.RongUserInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.view.CircleImageView;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import io.rong.eventbus.EventBus;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_friend_detail)
public class FriendDetailActivity extends BaseActivity {

    @ViewInject(R.id.user_icon)
    private CircleImageView iconView;
    @ViewInject(R.id.user_name)
    private TextView nameView;
    @ViewInject(R.id.add)
    private TextView addView;
    private String token;
    public static final String USER_INFO="user_info";
    public static final String USER_ID="user_id";
    private PPUserInfo ppUserInfo;
    private boolean isFriend;
    private IMUtils imUtils;
    private String friend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        imUtils = new IMUtils();
        token = SPUtils.get(LOGIN_TOKEN,"");
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String userId = intent.getStringExtra(USER_ID);
        if(TextUtils.isEmpty(userId)){
            ppUserInfo = (PPUserInfo) bundle.getSerializable(USER_INFO);
            showData();
        }else{
            searchUserInfo(token,userId);
        }

    }
    private void showData(){
        if(ppUserInfo.getUri()==null){
            Glide.with(getApplicationContext()).load(ppUserInfo.getIcon()).into(iconView);
        }else{
            Glide.with(this).load(ppUserInfo.getUri()).into(iconView);
        }

        nameView.setText(ppUserInfo.getName());
        imUtils.setOnAddListener(new IMUtils.OnAddListener() {
            @Override
            public void onFinished() {
                hideProgress();
            }

            @Override
            public void onSuccess(int type,String id,String name) {
                Toast.makeText(FriendDetailActivity.this,R.string.request_success,Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(FriendDetailActivity.this,msg,Toast.LENGTH_LONG).show();
            }
        });
        friend = IlessonApp.getInstance().getUserByPhone(ppUserInfo.getPhone());
        if(TextUtils.isEmpty(friend)&&!ppUserInfo.getPhone().equals(SPUtils.get(USER_PHONE,""))){
            addView.setText(R.string.add_to_contact);
        }else{
            addView.setText(R.string.send_msg);
        }
    }
    public void searchUserInfo(String token, final String userId) {
        final RequestParams params = new RequestParams(Constants.BASE_URL + Constants.RONG_URL);
        params.addBodyParameter("action", "info");
        params.addBodyParameter("token", token);
        params.addBodyParameter("target", userId);
        Log.d(TAG, "searchUserInfo: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: =" + result);
                BaseCode<RongUserInfo> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<RongUserInfo>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    RongUserInfo info = base.getData();
                    ppUserInfo = new PPUserInfo();
                    ppUserInfo.setIcon(info.getIcon());
                    ppUserInfo.setName(info.getName());
                    ppUserInfo.setPhone(userId);
                    showData();
                } else {
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
//    private boolean cantainUser(){
//        List<PPUserInfo> list = IlessonApp.getInstance().getDatas();
//        for(int i=0;i<list.size();i++){
//            PPUserInfo info = list.get(i);
//            if(info.getPhone().equals(ppUserInfo.getPhone())){
//                return true;
//            }
//        }
//        return false;
//    }
    private static final String TAG = "FriendDetailActivity";
    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }
    @Event(value = R.id.user_icon)
    private void user_icon(View view) {
        if(ppUserInfo!=null){
            Intent intent = new Intent(this,ImageActivity.class);
            intent.putExtra(ImageActivity.PATH,ppUserInfo.getIcon());
            startActivity(intent);
        }
    }
    @Event(value = R.id.add)
    private void add(View view) {
        if(!TextUtils.isEmpty(friend)){
            EventBus.getDefault().post(new Conversation());
            RongIM.getInstance().startConversation(mContext, Conversation.ConversationType.PRIVATE,ppUserInfo.getPhone(),ppUserInfo.getName());
        }else{
            showProgress();
            imUtils.addFriend(token,ppUserInfo.getPhone());
        }
    }
    private void addFriend() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addBodyParameter("action", "friend_add");
        params.addBodyParameter("token", token);
        params.addBodyParameter("method", "1");
        params.addBodyParameter("content", "hello");
        params.addBodyParameter("target", ppUserInfo.getPhone());
        Log.d(TAG, "loadData: "+params.toString());
        showProgress();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: "+result);
                BaseCode base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode>() {
                        }.getType());
                if (base.getCode() == 0) {
                    Toast.makeText(FriendDetailActivity.this,"请求成功",Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(FriendDetailActivity.this,"请求失败",Toast.LENGTH_LONG).show();
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
}
