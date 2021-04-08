package com.ilesson.ppim.activity;

import android.os.Bundle;

import android.view.View;

import com.ilesson.ppim.R;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
@ContentView(R.layout.subconversationlist)
public class SubConversationListActivtiy extends BaseActivity {
    private String mTargetId,title;

    boolean isFromPush = false;

    /**
     * 会话类型
     */
    private Conversation.ConversationType mConversationType;
    @Event(value = R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightMode(this,true);
    }


    /**
     * 展示如何从 Intent 中得到 融云会话页面传递的 Uri
     *//*
    private void getIntentDate(Intent intent) {
        mTargetId = intent.getData().getQueryParameter("targetId");
        title = intent.getData().getQueryParameter("title");
        Toast.makeText(this, title+"<<<会话ID>>>>>>>>>" + mTargetId, Toast.LENGTH_SHORT).show();

        //intent.getData().getLastPathSegment();//获得当前会话类型
        mConversationType = Conversation.ConversationType.valueOf(intent.getData().getLastPathSegment().toUpperCase(Locale.getDefault()));

//        enterFragment(mConversationType, mTargetId);
    }


    *//**
     * 加载会话页面 ConversationFragment
     *
     * @param mConversationType
     * @param mTargetId
     *//*
    private void enterFragment(Conversation.ConversationType mConversationType, String mTargetId) {

        ConversationFragment fragment = (ConversationFragment) getSupportFragmentManager().findFragmentById(R.id.conversation);

        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                .appendPath("conversation").appendPath(mConversationType.getName().toLowerCase())
                .appendQueryParameter("targetId", mTargetId).build();

        fragment.setUri(uri);
    }

    *//**
     * 重连
     *
     * @param token
     *//*
    private void reconnect(String token) {
        Log.e("", "《重连》");

        if (getApplicationInfo().packageName.equals(IlessonApp.getCurProcessName(getApplicationContext()))) {

            RongIM.connect(token, new RongIMClient.ConnectCallback() {
                @Override
                public void onTokenIncorrect() {
                    Log.e("", "连接失败");
                }

                @Override
                public void onSuccess(String s) {
                    enterFragment(mConversationType, mTargetId);
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    Log.e("", "连接失败—————>" + errorCode);
                }
            });
        }
    }


    *//**
     * 判断消息是否是 push 消息
     *//*
    private void isReconnect(Intent intent) {
        String token = IlessonApp.token1;

        if (intent == null || intent.getData() == null)
            return;
        //push
        if (intent.getData().getScheme().equals("rong") && intent.getData().getQueryParameter("isFromPush") != null) {
            isFromPush = true;
            Log.e("","isFromPush");
            //通过intent.getData().getQueryParameter("push") 为true，判断是否是push消息
            if (intent.getData().getQueryParameter("isFromPush").equals("true")) {
                reconnect(token);
            } else {
                //程序切到后台，收到消息后点击进入,会执行这里
                if (RongIM.getInstance() == null || RongIM.getInstance().getRongIMClient() == null) {
                    reconnect(token);
                } else {
                    enterFragment(mConversationType, mTargetId);
                }
            }
        }
    }
*/

//    @Override
//    public void onBackPressed() {
//        ConversationFragment fragment = (ConversationFragment) getSupportFragmentManager().findFragmentById(R.id.conversation);
//        if(!fragment.onBackPressed()) {
//            finish();
//        }
//    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (KeyEvent.KEYCODE_BACK == event.getKeyCode() && isFromPush) {
//            startActivity(new Intent(this, TestActivity.class));
//            finish();
//        }
//        return super.onKeyDown(keyCode, event);
//    }

}