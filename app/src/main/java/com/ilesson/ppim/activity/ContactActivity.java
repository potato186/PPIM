package com.ilesson.ppim.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.contactview.ContactAdapter;
import com.ilesson.ppim.contactview.DividerItemDecoration;
import com.ilesson.ppim.contactview.LetterView;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongIM;

import static com.ilesson.ppim.activity.ChatInfoActivity.GROUP_ID;
import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;
import static com.ilesson.ppim.activity.LoginActivity.USER_NAME;

/**
 * Created by potato on 2020/3/11.
 */
@ContentView(R.layout.activity_contact)
public class ContactActivity extends BaseActivity {
    @ViewInject(R.id.recylerview)
    private RecyclerView contactList;
    @ViewInject(R.id.letter_view)
    private LetterView letterView;
    @ViewInject(R.id.title)
    private TextView titleView;
    @ViewInject(R.id.confirm)
    private TextView confirm;
    private LinearLayoutManager layoutManager;

    private ContactAdapter adapter;
    private List<PPUserInfo> datas;
    private List<PPUserInfo> hasMembers;
    private IMUtils imUtils;
    @Event(R.id.back_btn)
    private void back(View v){
        finish();
    }
    @Event(R.id.confirm)
    private void confirm(View v){
        if(selects.isEmpty()){
            return;
        }
        if(type==GROUP_TYPE){
            startGroupChat();
            return;
        }
        if(type==INVATE_GROUP_TYPE){
            showProgress();
            imUtils.requestGroupChat(token,groupId,getMember(),memberName);
            return;
        }
        if(type==REMOVE_GROUP_TYPE){
            showDeleteDialog();
        }
    }
    private static final String TAG = "ContactActivity";
    public static final String SELECT_ACTION = "select_action";
    public static final String HAS_MEMBERS = "has_members";
    public static final int MSG_TYPE = 0;
    public static final int GROUP_TYPE = 1;
    public static final int INVATE_GROUP_TYPE = 2;
    public static final int REMOVE_GROUP_TYPE = 3;
    public static final int PAY_TYPE = 30;
    public static final int REMOVE_RESULT = 31;
    private int type;
    private String groupId;
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
        imUtils = new IMUtils();
        token = SPUtils.get(LOGIN_TOKEN, "");
        layoutManager = new LinearLayoutManager(this);
        type = getIntent().getIntExtra(SELECT_ACTION,0);
        groupId = getIntent().getStringExtra(GROUP_ID);
        datas = new ArrayList<>();
        hasMembers =  (ArrayList<PPUserInfo>) getIntent().getSerializableExtra(HAS_MEMBERS);
        contactList.setLayoutManager(layoutManager);
        contactList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        letterView.setCharacterListener(new LetterView.CharacterClickListener() {
            @Override
            public void clickCharacter(String character) {
                layoutManager.scrollToPositionWithOffset(adapter.getScrollPosition(character), 0);
            }
            @Override
            public void clickArrow() {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        });
        if(type==REMOVE_GROUP_TYPE){
            showListMember(hasMembers);
        }else{
            requestFriendsList();
        }
        if(type==GROUP_TYPE){
            titleView.setText(R.string.request_group);
        }else if(type==INVATE_GROUP_TYPE){
            titleView.setText(R.string.select_friends);
        }else if(type==REMOVE_GROUP_TYPE){
            titleView.setText(String.format(getResources().getString(R.string.group_members),hasMembers.size()));
            confirm.setText(R.string.delete);
        }
        imUtils.setOnAddListener(new IMUtils.OnAddListener() {
            @Override
            public void onFinished() {
                hideProgress();
            }

            @Override
            public void onSuccess(int type,String id,String names) {
                finish();
                imUtils.sendTextMsg(groupId,"欢迎"+names+"进入群聊");
                RongIM.getInstance().startGroupChat(ContactActivity.this, groupId, groupName);
            }
            @Override
            public void onFail(String msg) {
                Toast.makeText(ContactActivity.this,msg,Toast.LENGTH_LONG).show();
            }
        });
    }

    private String groupName;
    private String memberName;
    private String getMember(){
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        StringBuilder members = new StringBuilder();
        String name = SPUtils.get(USER_NAME,"");
        sb.append(name).append("、");
        for(int i=0;i<selects.size();i++){
            stringBuilder.append(selects.get(i).getPhone());
            stringBuilder.append("|");
            sb.append(selects.get(i).getName());
            members.append(selects.get(i).getName());
            sb.append("、");
            members.append("、");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        sb.deleteCharAt(sb.length()-1);
        members.deleteCharAt(members.length()-1);
        groupName = sb.toString();
        memberName = members.toString();
        return stringBuilder.toString();
    }
    private void requestFriendsList() {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.USER_URL);
        params.addBodyParameter("action", "friend");
        params.addBodyParameter("token", token);
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<List<PPUserInfo>> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<List<PPUserInfo>>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    List<PPUserInfo> data = base.getData();
                    if (null == data || data.isEmpty()) {
                        return;
                    }
                    if (null != hasMembers&&hasMembers.size()>0) {
                        data.removeAll(hasMembers);
                    }
                    showListMember(data);
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
    private void showListMember(List<PPUserInfo> data){
        datas.addAll(data);
        contactList.setVisibility(View.VISIBLE);
        letterView.setVisibility(View.VISIBLE);
        adapter = new ContactAdapter(ContactActivity.this, datas);
        contactList.setAdapter(adapter);
        setAdapterLinstener();
        if(type==GROUP_TYPE||type==INVATE_GROUP_TYPE||type==REMOVE_GROUP_TYPE){
            adapter.setShowCheck(true);
        }else if(type==PAY_TYPE){
            adapter.setOnlyTrans(true);
        }
    }
    private void startGroupChat() {
        confirm.setEnabled(false);
        String token = SPUtils.get(LOGIN_TOKEN, "");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.GROUP_URL);
        params.addBodyParameter("action", "create");
        params.addBodyParameter("token", token);
        params.addBodyParameter("member", getMember());
        params.addBodyParameter("name", groupName);
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<String> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<String>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    String groupId = base.getData();
                    RongIM.getInstance().startGroupChat(ContactActivity.this, groupId, groupName);
                    imUtils.sendTextMsg(groupId,"欢迎进入群聊");
                } else {
                    Toast.makeText(ContactActivity.this,base.getMessage(),Toast.LENGTH_LONG).show();
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
                confirm.setEnabled(true);
            }
        });
    }
    public void addGroupChat(String token,String groupId,String member) {
        //action=request&token=%s&group=%s&member=%s
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.GROUP_URL);
        params.addBodyParameter("action", "request");
        params.addBodyParameter("token", token);
        params.addBodyParameter("group", groupId);
        params.addBodyParameter("member", member);
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
//                    RongIM.getInstance().startGroupChat(ContactActivity.this, groupId, groupName);
                } else {
                    Toast.makeText(ContactActivity.this,base.getMessage(),Toast.LENGTH_LONG).show();
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
    public void removeMember() {
        ///pp/group?action=force_quit&token=%s&group=%s&member=%s
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.GROUP_URL);
        params.addBodyParameter("action", "force_quit");
        params.addBodyParameter("token", token);
        params.addBodyParameter("group", groupId);
        params.addBodyParameter("member", getMember());
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
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
                    intent.putExtra(HAS_MEMBERS, (Serializable) selects);
                    setResult(REMOVE_RESULT,intent);
                    finish();
                } else {
                    Toast.makeText(ContactActivity.this,base.getMessage(),Toast.LENGTH_LONG).show();
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
    private List<PPUserInfo> selects=new ArrayList<>();
    private void setAdapterLinstener(){
        adapter.setOnSelectChanger(members -> {
            selects = members;
            if(members.isEmpty()){
                if(type==REMOVE_GROUP_TYPE){
                    confirm.setText(R.string.delete);
                }else{
                    confirm.setText(R.string.rc_confirm);
                }
            }else{
                if(type==REMOVE_GROUP_TYPE){
                    confirm.setText(String.format(getResources().getString(R.string.delete_state),members.size()));
                }else{
                    confirm.setText(String.format(getResources().getString(R.string.select_state),members.size()));
                }
            }
        });
    }
    private void showDeleteDialog() {
        View view = getLayoutInflater().inflate(R.layout.practice_dialog, null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (PPScreenUtils.getScreenWidth(this) * 0.85);
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setAttributes(p);
        TextView scoreTv = (TextView) view.findViewById(R.id.content);
        scoreTv.setText(R.string.delete_member_tip);
        view.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeMember();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
