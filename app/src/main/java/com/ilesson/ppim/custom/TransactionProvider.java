package com.ilesson.ppim.custom;


import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.ConversationActivity;
import com.ilesson.ppim.activity.ExchangeInfoActivity;
import com.ilesson.ppim.activity.ForwadSelectActivity;
import com.ilesson.ppim.activity.LoginActivity;
import com.ilesson.ppim.utils.SPUtils;

import java.io.Serializable;

import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utilities.OptionsPopupDialog;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

/**
 * Created by longShun on 2017/2/24.
 * desc新建一个消息类继承 IContainerItemProvider.MessageProvider 类，实现对应接口方法，
 * 1.注意开头的注解！
 * 2.注意泛型！
 */
@ProviderTag(
        messageContent = TransactionMessage.class,//（这里是你自定义的消息实体）
        showReadState = true
)
public class TransactionProvider extends IContainerItemProvider.MessageProvider<TransactionMessage> {

    public TransactionProvider() {
    }
    private Context context;
    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        this.context = context;

        //这就是展示在会话界面的自定义的消息的布局
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction_message, null);
        ViewHolder holder = new ViewHolder();
        holder.name = (TextView) view.findViewById(R.id.name);
        holder.num = (TextView) view.findViewById(R.id.num);
        holder.layout = view.findViewById(R.id.layout);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, int i, TransactionMessage transactionMessage, UIMessage message) {

        //根据需求，适配数据
        ViewHolder holder = (ViewHolder) view.getTag();
        StringBuilder stringBuilder = new StringBuilder();

        if (message.getMessageDirection() == Message.MessageDirection.SEND) {//消息方向，自己发送的
            holder.layout.setBackgroundResource(R.mipmap.transaction_right);
        } else {
            holder.layout.setBackgroundResource(R.mipmap.transaction_left);
        }
        holder.name.setText(transactionMessage.getHasName()+"换"+transactionMessage.getNeedName());
        holder.num.setText(transactionMessage.getHasNum()+"换"+transactionMessage.getNeedNum());

    }
    private static final String TAG = "TransactionProvider";
    private static final String SPLIT = "//";

    @Override
    public Spannable getContentSummary(TransactionMessage TransactionMessage) {
//        Spannable spannable = null;
//        String content = TransactionMessage.getContent();
//        if(!content.contains(SPLIT)){
//            return null;
//        }
//        String[] arr = content.split(SPLIT);
//        String targetId = arr[1];
//        String myId = SPUtils.get(LoginActivity.USER_PHONE,"");
//        if (!targetId.equals(myId)) {
//            spannable = new SpannableString("[转消费积分]朋友已收");
//        }else{
//            spannable = new SpannableString("[转消费积分]你已收");
//        }
        return new SpannableString("[换]");
    }

    @Override
    public void onItemClick(View view, int i, TransactionMessage transactionMessage, UIMessage uiMessage) {
        Log.d(TAG, "onItemClick: "+transactionMessage);
        Intent intent = new Intent(view.getContext(), ExchangeInfoActivity.class);
        intent.putExtra(ExchangeInfoActivity.EXCHANGE_INFO, transactionMessage);
        view.getContext().startActivity(intent);
    }

//    @Override
//    public void onItemLongClick(final View view, int i, final TransactionMessage TransactionMessage, final UIMessage message) {
//        //实现长按删除等功能，咱们直接复制融云其他provider的实现
//        String[] items1;//复制，删除
//        items1 = new String[]{ view.getContext().getResources().getString(io.rong.imkit.R.string.rc_dialog_item_message_delete), view.getContext().getResources().getString(io.rong.imkit.R.string.rc_dialog_item_message_forward)};
//        OptionsPopupDialog.newInstance(view.getContext(), items1).setOptionsPopupDialogListener(new OptionsPopupDialog.OnOptionsItemClickedListener() {
//            public void onOptionsItemClicked(int which) {
//                if (which == 0) {
//                    RongIM.getInstance().deleteMessages(new int[]{message.getMessageId()}, (RongIMClient.ResultCallback) null);
//                }else if (which == 1) {
//                    Intent intent = new Intent(context, ForwadSelectActivity.class);
//                    intent.putExtra("msg",TransactionMessage);
//                    context.startActivity(intent);
//                }
//            }
//        }).show();
//
//    }


    @Override
    public void onItemLongClickAction(View view, int position, UIMessage message) {
        super.onItemLongClickAction(view, position, message);
        
    }

    private static class ViewHolder {
        private TextView name, num;
        private View layout;
    }
}
