package com.ilesson.ppim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.ilesson.ppim.R;
import com.ilesson.ppim.custom.TransactionMessage;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

import static com.ilesson.ppim.activity.ChatInfoActivity.GROUP_ID;
import static com.ilesson.ppim.activity.LoginActivity.USER_NAME;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_exchange_edit)
public class EditExchangeActivity extends BaseActivity{
    @ViewInject(R.id.input_name)
    private EditText inputName;
    @ViewInject(R.id.input_num)
    private EditText inputNum;
    @ViewInject(R.id.input_need)
    private EditText inputNeed;
    @ViewInject(R.id.input_need_num)
    private EditText inputNeedNum;
    @ViewInject(R.id.input_des)
    private EditText inputDes;
    private String groupId;
    private String userId;
    private String userName;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        Intent intent = getIntent();
        groupId = intent.getStringExtra(GROUP_ID);
        userId = SPUtils.get(USER_PHONE,"");
        userName = SPUtils.get(USER_NAME,"");
    }

    @Event(R.id.back_btn)
    private void back_btn(View view){
        finish();
    }
    @Event(R.id.comfirm)
    private void comfirm(View view){
        String name = inputName.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            showToast("消费积分或物品名称不能为空");
            return;
        }
        String num = inputNum.getText().toString().trim();
        if(TextUtils.isEmpty(num)){
            showToast("消费积分或物品数量不能为空");
            return;
        }
        String need = inputNeed.getText().toString().trim();
        if(TextUtils.isEmpty(need)){
            showToast("要换的消费积分或物品名称不能为空");
            return;
        }
        String needNum = inputNeedNum.getText().toString().trim();
        if(TextUtils.isEmpty(needNum)){
            showToast("要换的消费积分或物品数量不能为空");
            return;
        }//(String hasName, String needName, int hasNum, int needNum, String des)
        String des = inputDes.getText().toString().trim();
        hideShowKeyboard();
        sendTransaction(groupId,new TransactionMessage(name,need,num,needNum,des,userId,userName));
    }
    public void hideShowKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //得到InputMethodManager的实例
        if (imm.isActive()) {//如果开启
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);//关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的
        }
    }

    public void sendTransaction(String tartgetId, TransactionMessage transactionMessage) {
        Message msg = Message.obtain(tartgetId, Conversation.ConversationType.GROUP, transactionMessage);

        RongIM.getInstance().sendMessage(msg,
                "transaction", null, new IRongCallback.ISendMessageCallback() {
                    @Override
                    public void onAttached(Message message) {

                    }

                    @Override
                    public void onSuccess(Message message) {
                        finish();
                    }

                    @Override
                    public void onError(Message message, RongIMClient.ErrorCode errorCode) {

                    }
                });
    }
}
