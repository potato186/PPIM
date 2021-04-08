package com.ilesson.ppim.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.CollectActivity;
import com.ilesson.ppim.activity.ForwadSelectActivity;
import com.ilesson.ppim.custom.ComposeMessage;

import io.rong.eventbus.EventBus;
import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

import static com.ilesson.ppim.activity.ForwadSelectActivity.COMPOSEMESSAGE;
import static com.ilesson.ppim.activity.OutlineActivity.conversationType;
import static com.ilesson.ppim.activity.OutlineActivity.targetId;

public class ComposeMsgUtils {
    private static final String TAG = "ComposeMsgUtils";
    public ComposeMsgUtils() {
    }
    private Context context;
    public ComposeMsgUtils(Context context) {
        this.context = context;
    }
    public void showSendDialog(final Context context, final ComposeMessage message) {
        final Dialog mSendDialog = new Dialog(context);
        mSendDialog.setCanceledOnTouchOutside(true);
        mSendDialog.setCancelable(true);
        Window window = mSendDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        View view = View.inflate(context, R.layout.select_send_item, null);
        view.findViewById(R.id.send_current).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendDialog.dismiss();
//                ComposeMessage message = new ComposeMessage();
//                message.setTitle(title);
//                message.setUuid(uuid);
//                message.setCount(wordNum);
//                message.setGrade(grade);
//                message.setScore(score);
                io.rong.imlib.model.Message msg = io.rong.imlib.model.Message.obtain(targetId, conversationType, message);
                RongIM.getInstance().sendMessage(msg,
                        "", null, new IRongCallback.ISendMessageCallback() {
                            @Override
                            public void onAttached(io.rong.imlib.model.Message message) {
                                Log.d(TAG, "onAttached: "+message);
                            }

                            @Override
                            public void onSuccess(io.rong.imlib.model.Message message) {
                                EventBus.getDefault().post(new Conversation());
                                RongIM.getInstance().startConversation(context, conversationType, targetId, "");
                            }

                            @Override
                            public void onError(io.rong.imlib.model.Message message, RongIMClient.ErrorCode errorCode) {
                                Log.d(TAG, "onError: "+errorCode);
                            }
                        });

            }
        });
        view.findViewById(R.id.send_to_other).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendDialog.dismiss();
                Intent intent = new Intent(context, ForwadSelectActivity.class);
                intent.putExtra("msg",message);
                context.startActivity(intent);
            }
        });
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendDialog.dismiss();
            }
        });
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        mSendDialog.show();
    }
}
