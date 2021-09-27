package com.ilesson.ppim.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.adapter.BlacklistAdapter;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.entity.RongUserInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;

/**
 * Created by potato on 2020/3/10.
 */
@ContentView(R.layout.activity_black_list)
public class BlackListActivity extends BaseActivity {

    @ViewInject(R.id.chat_record)
    private RecyclerView recyclerView;
    @ViewInject(R.id.empty_layout)
    private View emptyLayout;
    private List<PPUserInfo> allFriends;
    private List<PPUserInfo> result;
    private String token;
    private MessageContent messageContent;
    private boolean otherFile;
    private Intent intent;
    private String targetId;
    private String targetName;
    private Uri uri;
    private List<Uri> uris = new ArrayList<>();
    private Conversation.ConversationType conversationType;
    private BlacklistAdapter adapter;
    private List<RongUserInfo> userInfos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        allFriends = new ArrayList<>();
        result = new ArrayList<>();
        userInfos = new ArrayList<>();
        adapter = new BlacklistAdapter(this, userInfos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        token = SPUtils.get(LOGIN_TOKEN,"");
        blackListQuery();
    }

    private static final String TAG = "BlackListActivity";
    private void blackListQuery() {
        String token = SPUtils.get(LoginActivity.LOGIN_TOKEN,"");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addParameter("action", "black_list_query");
        params.addParameter("token", token);
        params.addParameter("target", SPUtils.get(LoginActivity.USER_PHONE,""));
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
                    if(null==list||list.size()==0){
                        showEmpty();
                    }else{
                        userInfos.addAll(list);
                        adapter.notifyDataSetChanged();
                    }
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
                hideProgress();
            }
        });
    }
    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }
    private void showEmpty(){
        recyclerView.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }
}
