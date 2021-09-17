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
import com.ilesson.ppim.entity.FreshConversation;
import com.ilesson.ppim.entity.PPUserInfo;
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
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.AvatarActivity.MODIFY_SUCCESS;
import static com.ilesson.ppim.activity.FriendDetailActivity.USER_ID;


/**
 * Created by potato on 2019/4/9.
 */
@ContentView(R.layout.act_chat_user_info)
public class UserSttingActivity extends BaseActivity {
    @ViewInject(R.id.disturb_switch)
    private Switch disturbSwitch;
    @ViewInject(R.id.message_top_switch)
    private Switch messageTopSwitch;
    private static final String TAG = "UserSttingActivity";
    private String name;
    private String nikeName = "";
    private String targetId;
    private String targetName;
    private boolean modifyed;
    private PPUserInfo addUser, deleteUser;
    private String groupIcon;
    public static boolean isTop;

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setStatusBarLightMode(this, true);
        targetId = getIntent().getStringExtra(USER_ID);
        RongIMClient.getInstance().getConversationNotificationStatus(Conversation.ConversationType.PRIVATE, targetId, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            /**
             * 成功回调
             * @param status 消息提请状态
             */
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus status) {
                disturbSwitch.setChecked(status == Conversation.ConversationNotificationStatus.NOTIFY ? false : true);
                disturbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Conversation.ConversationNotificationStatus notificationStatus = isChecked ? Conversation.ConversationNotificationStatus.DO_NOT_DISTURB : Conversation.ConversationNotificationStatus.NOTIFY;
                        RongIMClient.getInstance().setConversationNotificationStatus(Conversation.ConversationType.PRIVATE, targetId, notificationStatus, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {

                            /**
                             * 成功回调
                             * @param status 消息提请状态
                             */
                            @Override
                            public void onSuccess(Conversation.ConversationNotificationStatus status) {
                                EventBus.getDefault().post(new FreshConversation());
                                RongIM.getInstance().setConversationNotificationStatus(Conversation.ConversationType.PRIVATE,
                                        targetId, status, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
                                            @Override
                                            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                                            }

                                            @Override
                                            public void onError(RongIMClient.ErrorCode errorCode) {
                                            }
                                        });
                            }

                            /**
                             * 错误回调
                             * @param errorCode 错误码
                             */
                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                Log.d(TAG, "onError: " + errorCode.getMessage());
                            }
                        });
                    }
                });
            }

            /**
             * 错误回调
             * @param errorCode 错误码
             */
            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });

        messageTopSwitch.setChecked(isTop);
        messageTopSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isTop = isChecked;
                RongIMClient.getInstance().setConversationToTop(Conversation.ConversationType.PRIVATE, targetId, isChecked, false, new
                        RongIMClient.ResultCallback<Boolean>() {
                            /**
                             * 成功回调
                             */
                            @Override
                            public void onSuccess(Boolean success) {
                                EventBus.getDefault().post(new FreshConversation());
                            }

                            /**
                             * 错误回调
                             */
                            @Override
                            public void onError(RongIMClient.ErrorCode ErrorCode) {

                            }
                        });
            }
        });
    }

    @Event(R.id.back_btn)
    private void back_btn(View view) {
        finish();
    }
    @Event(R.id.search_record)
    private void search_record(View view){
        Intent intent = new Intent(this,SearchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ConversationActivity.CONVERSATION_TYPE, Conversation.ConversationType.PRIVATE);
        intent.putExtras(bundle);
        intent.putExtra(ConversationActivity.TARGET_ID,targetId);
//        intent.putExtra(ConversationActivity.TARGET_NAME,name);
        startActivity(intent);
    }
    private int requstCode;

    @Event(R.id.quit)
    private void quit(View view) {
        showQuitDialog();
    }

    @Event(R.id.delete)
    private void delete(View view) {
        showDeleteDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MODIFY_SUCCESS) {
        }
    }

    private void showQuitDialog() {
        View view = getLayoutInflater().inflate(R.layout.practice_dialog, null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
//        TextView title = (TextView) view.findViewById(R.id.title);
        TextView scoreTv = (TextView) view.findViewById(R.id.content);
        scoreTv.setText(R.string.quit_tip);
        view.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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
//        TextView title = (TextView) view.findViewById(R.id.title);
        TextView scoreTv = (TextView) view.findViewById(R.id.content);
        scoreTv.setText(R.string.delete_group_tip);
        view.findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void requestGroupInfo() {
        ///pp/group?action=info&token=%s&group=%s
        String token = SPUtils.get(LoginActivity.LOGIN_TOKEN, "");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.GROUP_URL);
        params.addParameter("action", "list");
        params.addParameter("token", token);
        params.addParameter("page", 0);
        params.addParameter("size", 20000);
//        params.addParameter("group", userId);
        showProgress();
        Log.d(TAG, "requestGroupInfo: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: =" + result);
                BaseCode<List<PPUserInfo>> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<List<PPUserInfo>>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    List<PPUserInfo> list = base.getData();
                } else {
                    Toast.makeText(UserSttingActivity.this, base.getMessage(), Toast.LENGTH_LONG).show();
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

}
