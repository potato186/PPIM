package com.ilesson.ppim.custom;


import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.WebActivity;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.CheckRedpacketData;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.SPUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utilities.OptionsPopupDialog;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

import static com.ilesson.ppim.activity.LoginActivity.LOGIN_TOKEN;

/**
 * Created by longShun on 2017/2/24.
 * desc新建一个消息类继承 IContainerItemProvider.MessageProvider 类，实现对应接口方法，
 * 1.注意开头的注解！
 * 2.注意泛型！
 */
@ProviderTag(
        messageContent = ComposeMessage.class,//（这里是你自定义的消息实体）
        showReadState = true
)
public class ComposeItemProvider extends IContainerItemProvider.MessageProvider<ComposeMessage> {

    public ComposeItemProvider() {
    }

    private Context context;

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        this.context = context;

        //这就是展示在会话界面的自定义的消息的布局
        View view = LayoutInflater.from(context).inflate(R.layout.item_compose_message, null);
        ViewHolder holder = new ViewHolder();
        holder.title = (TextView) view.findViewById(R.id.title);
        holder.grade = (TextView) view.findViewById(R.id.grade);
        holder.count = (TextView) view.findViewById(R.id.count);
        holder.score = (TextView) view.findViewById(R.id.content);
        holder.layout = view.findViewById(R.id.layout);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, int i, ComposeMessage msg, UIMessage message) {

        //根据需求，适配数据
        ViewHolder holder = (ViewHolder) view.getTag();

        if (message.getMessageDirection() == Message.MessageDirection.SEND) {//消息方向，自己发送的
            holder.layout.setBackgroundResource(R.drawable.rc_ic_bubble_right_file);
        } else {
            holder.layout.setBackgroundResource(R.drawable.rc_ic_bubble_left_file);
        }
        holder.title.setText(msg.getTitle());
        if (TextUtils.isEmpty(msg.getCount())) {
            holder.count.setVisibility(View.GONE);
        }
        holder.count.setText("字数:" + msg.getCount() + "字");
        holder.grade.setText("年级:" + msg.getGrade() + "年级");
        Log.d(TAG, "bindView: " + msg);
        if (!TextUtils.isEmpty(msg.getScore())) {
            try {
                int score = Integer.valueOf(msg.getScore());
                if (score < 60) {
                    holder.score.setBackgroundResource(R.drawable.background_standard_bad_corner5);
                } else {
                    holder.score.setBackgroundResource(R.drawable.background_standard_good_corner5);
                }
                holder.score.setText(msg.getScore());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final String TAG = "RedPackageItemProvider";

    @Override
    public Spannable getContentSummary(ComposeMessage redPackageMessage) {
        return new SpannableString("[文如其人]");
    }

    //    @Override
//    public Spannable getSummary(UIMessage message) {
//        Spannable spannable = null;
//        String targetId = message.getTargetId();
//        String myId = SPUtils.get(LoginActivity.USER_PHONE,"");
//        Log.d(TAG, "getSummary: targetId="+targetId);
//        if (!targetId.equals(myId)) {
//            spannable = new SpannableString("[转消费积分]朋友已收");
//        }else{
//            spannable = new SpannableString("[转消费积分]你已收");
//        }
//        return spannable;
//    }
//<string name="transfer_to">转账给</string>
//    <string name="transfer_to_you">转账给你</string>
//    <string name="transfer_summary">[转账]请你确认收款</string>
    private boolean loading;

    @Override
    public void onItemClick(View view, int i, ComposeMessage msg, UIMessage uiMessage) {
        Intent intent = new Intent(view.getContext(), WebActivity.class);
        intent.putExtra(WebActivity.WORD_NUM, msg.getCount());
        intent.putExtra(WebActivity.TITLE, msg.getTitle());
        intent.putExtra(WebActivity.UUID, msg.getUuid());
        intent.putExtra(WebActivity.SCORE, msg.getScore());
        intent.putExtra(WebActivity.GRADE, msg.getGrade());
        view.getContext().startActivity(intent);
    }

    private static class ViewHolder {
        private TextView title, grade, count, score;
        private View layout;
    }

    private void check(final String groupId, final ComposeMessage message, final View view, final UIMessage uiMessage) {
        loading = true;
        final String token = SPUtils.get(LOGIN_TOKEN, "");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.MONEY_URL);
        params.addBodyParameter("action", "has");
        params.addBodyParameter("token", token);
//        params.addBodyParameter("id", message.getRedpacketId());
        params.addBodyParameter("group", groupId);
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: " + result);
                BaseCode<CheckRedpacketData> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<CheckRedpacketData>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    CheckRedpacketData data = base.getData();
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
                loading = false;
            }
        });
    }

    @Override
    public void onItemLongClick(View view, int position, final ComposeMessage content, final UIMessage message) {
        String[] items1;
        items1 = new String[]{view.getContext().getResources().getString(io.rong.imkit.R.string.rc_dialog_item_message_delete)};
        OptionsPopupDialog.newInstance(view.getContext(), items1).setOptionsPopupDialogListener(new OptionsPopupDialog.OnOptionsItemClickedListener() {
            public void onOptionsItemClicked(int which) {
                if (which == 0) {
                    RongIM.getInstance().deleteMessages(new int[]{message.getMessageId()}, (RongIMClient.ResultCallback) null);
                }
            }
        }).show();
    }
}
