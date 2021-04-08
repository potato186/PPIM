package com.ilesson.ppim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.custom.TransactionMessage;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import io.rong.eventbus.EventBus;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_exchange_info)
public class ExchangeInfoActivity extends BaseActivity{
    @ViewInject(R.id.user1)
    private TextView user1;
    @ViewInject(R.id.userNeed)
    private TextView userNeed;
    @ViewInject(R.id.name)
    private TextView name;
    @ViewInject(R.id.num)
    private TextView num;
    @ViewInject(R.id.need)
    private TextView need;
    @ViewInject(R.id.needNum)
    private TextView needNum;
    @ViewInject(R.id.des)
    private TextView des;
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.comfirm)
    private View comfirm;
    private String groupId;
    public static final String EXCHANGE_INFO = "exchange_info";
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    private String userId;
    private String userName;
    private TransactionMessage message;
    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this,true);
        Intent intent = getIntent();
        message = (TransactionMessage) intent.getParcelableExtra(EXCHANGE_INFO);
        user1.setText(message.getUserName()+"出");
        userNeed.setText(message.getUserName()+"换");
        name.setText(message.getHasName());
        num.setText(message.getHasNum());
        need.setText(message.getNeedName());
        needNum.setText(message.getNeedNum());
        des.setText(message.getDes());
        title.setText(message.getUserName()+"-换");
        if(SPUtils.get(USER_PHONE,"").equals(message.getUserId())){
            comfirm.setVisibility(View.GONE);
        }
    }

    private static final String TAG = "ExchangeInfoActivity";
    @Event(R.id.back_btn)
    private void back_btn(View view){
        finish();
    }
    @Event(R.id.comfirm)
    private void comfirm(View view){
        Log.d(TAG, "comfirm: "+message);
        EventBus.getDefault().post(new Conversation());
        RongIM.getInstance().startConversation(this, Conversation.ConversationType.PRIVATE,message.getUserId(),message.getUserName());
    }
}
